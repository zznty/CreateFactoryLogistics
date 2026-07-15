package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.repackager.CompositeRepackagerHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class RepackagerGameTests {

    // --- basic sanity ---

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void singleItemPackageIsRepackagedIntact(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 1);
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 1,
                "single-item package should yield 1 item, got " + itemCount(result)
                        + " in " + result.size() + " boxes");
        helper.succeed();
    }

    // --- recipe / cascade edge cases ---

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void recipePartialConsumptionDoesNotLoseItems(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 1);
        setOrderWithRecipe(be, pkg, 1,
                recipe(iron(1), redstone(1)));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 1,
                "only 1 ingredient present, should preserve all items, got " + itemCount(result));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void fullRecipeProducesOutputAndNoLeftovers(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 1, Items.REDSTONE, 1);
        setOrderWithRecipe(be, pkg, 1,
                recipe(iron(1), redstone(1)));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1, "should produce exactly 1 recipe box");
        helper.assertValueEqual(itemCount(result), 2,
                "recipe box should contain both ingredients, got " + itemCount(result));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void multiPackageOrderAggregatesForRecipe(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg1 = makePackage(Items.IRON_INGOT, 1);
        ItemStack pkg2 = makePackage(Items.REDSTONE, 1);
        setOrderWithRecipe(be, pkg1, 1,
                recipe(iron(1), redstone(1)));
        PackageItem.addAddress(pkg1, "test");
        PackageItem.addAddress(pkg2, "test");
        putPackages(rh, 1, List.of(pkg1, pkg2));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1, "should produce 1 recipe box from 2 packages");
        helper.assertValueEqual(itemCount(result), 2,
                "recipe box should aggregate both ingredients, got " + itemCount(result));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void recipeWithLeftoverNonRecipeItems(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 2, Items.REDSTONE, 1, Items.COAL, 3);
        setOrderWithRecipe(be, pkg, 1,
                recipe(iron(1), redstone(1)));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 6,
                "1 recipe box (2 items) + leftover 1 iron + 3 coal = 6 total, got " + itemCount(result)
                        + " across " + result.size() + " boxes");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void multipleRecipesOneIncomplete(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 2, Items.REDSTONE, 1, Items.COAL, 1);
        PackageOrderWithCrafts order = new PackageOrderWithCrafts(PackageOrder.empty(),
                List.of(
                        new CraftingEntry(new PackageOrder(recipe(iron(1), redstone(1))), 1),
                        new CraftingEntry(new PackageOrder(recipe(new BigItemStack(new ItemStack(Items.IRON_INGOT), 1),
                                new BigItemStack(new ItemStack(Items.COAL), 2))), 1)));
        setOrder(be, pkg, 1, GenericOrder.of(order));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        // recipe 1 fulfilled: iron x1, redstone x1
        // recipe 2 incomplete: iron x1, coal x1 → preserved as non-recipe items
        helper.assertValueEqual(itemCount(result), 4,
                "1 recipe box (2 items) + 2 leftover items = 4, got " + itemCount(result)
                        + " across " + result.size() + " boxes");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void recipeWithMultiCraftCount(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 3, Items.REDSTONE, 3);
        PackageOrderWithCrafts order = new PackageOrderWithCrafts(PackageOrder.empty(),
                List.of(new CraftingEntry(new PackageOrder(recipe(iron(1), redstone(1))), 3)));
        setOrder(be, pkg, 1, GenericOrder.of(order));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 6,
                "3 recipe packages × 2 items each = 6, got " + itemCount(result)
                        + " across " + result.size() + " boxes");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void noRecipeContextPacksEverythingPlain(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 5, Items.GOLD_INGOT, 3);
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 8,
                "no recipe → plain repack 8 items, got " + itemCount(result)
                        + " across " + result.size() + " boxes");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void emptyOrderProducesNothing(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);
        putPackages(rh, 1, List.of());

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 0,
                "empty collected order should yield no packages, got " + result.size());
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void recipeRequiresExactlyWhatIsAvailable(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg = makePackage(Items.IRON_INGOT, 1, Items.REDSTONE, 1);
        setOrderWithRecipe(be, pkg, 1,
                recipe(iron(1), redstone(1)));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1, "exact match should produce 1 recipe box, got " + result.size());
        helper.assertValueEqual(itemCount(result), 2,
                "box should have both items, got " + itemCount(result));
        helper.succeed();
    }

    // --- helpers ---

    private static RepackagerBlockEntity placeRepackager(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, AllBlocks.REPACKAGER.getDefaultState());
        return helper.getBlockEntity(pos);
    }

    private static CompositeRepackagerHelper helper(RepackagerBlockEntity be) {
        return (CompositeRepackagerHelper) be.repackageHelper;
    }

    private static void clearPackages(CompositeRepackagerHelper rh) {
        try {
            Field f = rh.getClass().getSuperclass().getSuperclass().getDeclaredField("collectedPackages");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, List<ItemStack>> collected = (Map<Integer, List<ItemStack>>) f.get(rh);
            collected.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void putPackages(CompositeRepackagerHelper rh, int orderId, List<ItemStack> packages) {
        try {
            Field f = rh.getClass().getSuperclass().getSuperclass().getDeclaredField("collectedPackages");
            f.setAccessible(true);
            Map<Integer, List<ItemStack>> collected = (Map<Integer, List<ItemStack>>) f.get(rh);
            collected.put(orderId, new ArrayList<>(packages));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemStack makePackage(Object... items) {
        ItemStackHandler contents = new ItemStackHandler(PackageItem.SLOTS);
        int slot = 0;
        for (int i = 0; i < items.length; i += 2)
            contents.setStackInSlot(slot++, new ItemStack((net.minecraft.world.level.ItemLike) items[i], (int) items[i + 1]));
        return PackageItem.containing(contents);
    }

    private static void setOrderWithRecipe(RepackagerBlockEntity be, ItemStack pkg, int orderId,
                                           List<BigItemStack> recipe) {
        setOrder(be, pkg, orderId, GenericOrder.of(PackageOrderWithCrafts.singleRecipe(recipe)));
    }

    private static void setOrder(RepackagerBlockEntity be, ItemStack pkg, int orderId, GenericOrder order) {
        GenericOrder.set(be.getLevel().registryAccess(), pkg, orderId, 0, true, 0, true, order);
    }

    private static List<BigItemStack> recipe(BigItemStack... stacks) {
        return List.of(stacks);
    }

    private static BigItemStack iron(int count) {
        return new BigItemStack(new ItemStack(Items.IRON_INGOT), count);
    }

    private static BigItemStack redstone(int count) {
        return new BigItemStack(new ItemStack(Items.REDSTONE), count);
    }

    private static int itemCount(List<BigItemStack> boxes) {
        int total = 0;
        for (BigItemStack box : boxes) {
            ItemStackHandler contents = PackageItem.getContents(box.stack);
            for (int slot = 0; slot < contents.getSlots(); slot++)
                total += contents.getStackInSlot(slot).getCount() * box.count;
        }
        return total;
    }
}
