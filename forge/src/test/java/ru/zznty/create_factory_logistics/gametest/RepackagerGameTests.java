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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackageBuilder;
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

    // --- #241 server hang: duplicate craft ingredients leave negative summary amounts ---

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void recipeWithDuplicateIngredientsDoesNotHang(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        // Pattern uses iron twice; only 1 iron present. Two-pass validation used to treat this
        // as craftable, subtract past zero, then spin forever in the leftover repack loop.
        ItemStack pkg = makePackage(Items.IRON_INGOT, 1);
        setOrderWithRecipe(be, pkg, 1, recipe(iron(1), iron(1)));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 1,
                "duplicate-ingredient craft with insufficient items must preserve the iron, got "
                        + itemCount(result));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void recipeWithDuplicateIngredientsAndExactCountDoesNotHang(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        // 3 iron for pattern [iron, iron] with craft count 2: second craft would drive amount negative
        // under the broken two-pass check (saw count twice without reserving).
        ItemStack pkg = makePackage(Items.IRON_INGOT, 3);
        PackageOrderWithCrafts order = new PackageOrderWithCrafts(PackageOrder.empty(),
                List.of(new CraftingEntry(new PackageOrder(recipe(iron(1), iron(1))), 2)));
        setOrder(be, pkg, 1, GenericOrder.of(order));
        PackageItem.addAddress(pkg, "test");
        putPackages(rh, 1, List.of(pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 3,
                "2 crafts of 2 iron need 4; with 3 iron only 1 craft + 1 leftover = 3 items, got "
                        + itemCount(result));
        helper.succeed();
    }

    // --- jar / fluid repackager tests (#159 / #156) ---

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void singleJarIsRepackagedIntact(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar = makeJar(Fluids.WATER, 1000);
        setOrder(be, jar, 1, GenericOrder.empty());
        putPackages(rh, 1, List.of(jar));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1, "single jar should yield 1 output, got " + result.size());
        FluidStack fluid = FluidUtil.getFluidContained(result.get(0).stack).orElse(FluidStack.EMPTY);
        helper.assertValueEqual(fluid.getAmount(), 1000,
                "output jar should contain 1000mb water, got " + fluid.getAmount());
        helper.assertTrue(FluidStack.isSameFluidSameComponents(fluid, new FluidStack(Fluids.WATER, 1)),
                "output fluid should be water");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void multipleJarsProduceCompositePackage(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar1 = makeJar(Fluids.WATER, 1000);
        ItemStack jar2 = makeJar(Fluids.LAVA, 1000);
        setOrder(be, jar1, 1, GenericOrder.empty());
        setOrder(be, jar2, 1, GenericOrder.empty());
        putPackages(rh, 1, List.of(jar1, jar2));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1,
                "two different jars should produce 1 composite, got " + result.size());
        List<ItemStack> children = CompositePackageItem.getChildren(be.getLevel().registryAccess(),
                                                                     result.get(0).stack);
        helper.assertValueEqual(children.size(), 2,
                "composite should have 2 children, got " + children.size());
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void jarAndItemPackageProduceComposite(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar = makeJar(Fluids.WATER, 1000);
        ItemStack pkg = makePackage(Items.IRON_INGOT, 4);
        setOrder(be, jar, 1, GenericOrder.empty());
        setOrder(be, pkg, 1, GenericOrder.empty());
        putPackages(rh, 1, List.of(jar, pkg));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1,
                "jar + item should produce 1 composite, got " + result.size());
        helper.assertTrue(result.get(0).stack.getItem() instanceof CompositePackageItem,
                "output should be a composite package");
        helper.succeed();
    }

    // --- same-type merge regression (#240) ---

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void multiplePackagesSameItemTypeMergeIntoOne(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg1 = makePackage(Items.IRON_INGOT, 16);
        ItemStack pkg2 = makePackage(Items.IRON_INGOT, 16);
        setOrder(be, pkg1, 1, GenericOrder.empty());
        setOrder(be, pkg2, 1, GenericOrder.empty());
        putPackages(rh, 1, List.of(pkg1, pkg2));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 32,
                "two 16-iron packages should merge to 32 items, got " + itemCount(result)
                        + " across " + result.size() + " boxes");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void multiplePackagesSameItemTypeNoRecipePreserveCount(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg1 = makePackage(Items.REDSTONE, 32);
        ItemStack pkg2 = makePackage(Items.REDSTONE, 32);
        ItemStack pkg3 = makePackage(Items.REDSTONE, 32);
        putPackages(rh, 1, List.of(pkg1, pkg2, pkg3));

        List<BigItemStack> result = rh.repack(1, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 96,
                "three 32-redstone packages should merge to 96 items, got " + itemCount(result)
                        + " across " + result.size() + " boxes");
        helper.succeed();
    }

    // --- fragment collection (#159 / #156 / #240 end-to-end via addPackageFragment) ---

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void isFragmentedTrueForJarWithFragment(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);

        ItemStack jar = makeJar(Fluids.WATER, 1000);
        GenericOrder.set(be.getLevel().registryAccess(), jar, 42, 0, true, 0, true, GenericOrder.empty());

        helper.assertTrue(rh.isFragmented(jar),
                "jar with Fragment tag should be marked as fragmented");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void isFragmentedFalseForUntaggedJar(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);

        ItemStack jar = makeJar(Fluids.WATER, 1000);

        helper.assertFalse(rh.isFragmented(jar),
                "jar without Fragment tag should not be marked as fragmented");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void addPackageFragmentAcceptsSingleJar(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar = makeJar(Fluids.WATER, 1000);
        GenericOrder.set(be.getLevel().registryAccess(), jar, 42, 0, true, 0, true, GenericOrder.empty());

        int result = rh.addPackageFragment(jar);
        helper.assertValueEqual(result, 42,
                "jar with IsFinal+IsFinalLink fragment should complete order, got " + result);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void addPackageFragmentRejectsUntaggedJar(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar = makeJar(Fluids.WATER, 1000);

        int result = rh.addPackageFragment(jar);
        helper.assertValueEqual(result, -1,
                "jar without Fragment tag should be rejected, got " + result);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void addPackageFragmentCollectsTwoJars(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar1 = makeJar(Fluids.WATER, 1000);
        ItemStack jar2 = makeJar(Fluids.LAVA, 1000);
        GenericOrder.set(be.getLevel().registryAccess(), jar1, 42, 0, true, 0, false, null);
        GenericOrder.set(be.getLevel().registryAccess(), jar2, 42, 0, true, 1, true, null);

        int r1 = rh.addPackageFragment(jar1);
        int r2 = rh.addPackageFragment(jar2);

        helper.assertValueEqual(r1, -1, "fragment 0 alone should not complete order");
        helper.assertTrue(r2 != -1, "fragment 1 should complete the order");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void collectThenRepackSingleJarViaAddFragment(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack jar = makeJar(Fluids.WATER, 1000);
        GenericOrder.set(be.getLevel().registryAccess(), jar, 42, 0, true, 0, true, GenericOrder.empty());

        int orderId = rh.addPackageFragment(jar);
        helper.assertTrue(orderId != -1, "jar should be collected");

        List<BigItemStack> result = rh.repack(orderId, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(result.size(), 1, "repacked single jar should yield 1 output, got " + result.size());
        FluidStack fluid = FluidUtil.getFluidContained(result.get(0).stack).orElse(FluidStack.EMPTY);
        helper.assertValueEqual(fluid.getAmount(), 1000,
                "output should contain 1000mb water, got " + fluid.getAmount());
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void collectTwoSameTypeItemsThenRepackViaAddFragment(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        ItemStack pkg1 = makePackage(Items.IRON_INGOT, 16);
        ItemStack pkg2 = makePackage(Items.IRON_INGOT, 16);
        GenericOrder.set(be.getLevel().registryAccess(), pkg1, 42, 0, true, 0, false, null);
        GenericOrder.set(be.getLevel().registryAccess(), pkg2, 42, 0, true, 1, true, null);

        int r1 = rh.addPackageFragment(pkg1);
        int r2 = rh.addPackageFragment(pkg2);

        helper.assertValueEqual(r1, -1, "fragment 0 alone should not complete order");
        helper.assertTrue(r2 != -1, "fragment 1 should complete the order");

        List<BigItemStack> result = rh.repack(r2, RandomSource.createNewThreadLocalInstance());
        helper.assertValueEqual(itemCount(result), 32,
                "two 16-iron packages should merge to 32 items via addPackageFragment, got "
                        + itemCount(result) + " across " + result.size() + " boxes");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "repackager", timeoutTicks = 80)
    public static void addPackageFragmentRejectsVanillaCreatePackage(GameTestHelper helper) {
        RepackagerBlockEntity be = placeRepackager(helper);
        CompositeRepackagerHelper rh = helper(be);
        clearPackages(rh);

        // vanilla Create package: items set via PackageItem.containing, no Fragment tag
        ItemStack pkg = makePackage(Items.IRON_INGOT, 8);

        helper.assertFalse(rh.isFragmented(pkg),
                "vanilla Create package without Fragment tag should not be fragmented");
        helper.assertValueEqual(rh.addPackageFragment(pkg), -1,
                "vanilla Create package should be rejected by addPackageFragment");
        helper.succeed();
    }

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

    private static ItemStack makeJar(Fluid fluid, int amount) {
        JarPackageBuilder builder = new JarPackageBuilder();
        builder.add(FluidGenericStack.wrap(new FluidStack(fluid, amount)));
        return builder.build();
    }
}
