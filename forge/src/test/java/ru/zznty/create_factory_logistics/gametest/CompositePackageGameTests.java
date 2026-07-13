package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.List;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class CompositePackageGameTests {
    @GameTest(template = "empty", batch = "composite_packages")
    public static void storesItemsAndChildren(GameTestHelper helper) {
        ItemStackHandler contents = new ItemStackHandler(PackageItem.SLOTS);
        contents.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));
        contents.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT, 3));
        ItemStack child = PackageStyles.getDefaultBox();
        ItemStack composite = CompositePackageItem.of(helper.getLevel().registryAccess(),
                PackageItem.containing(contents), List.of(child));

        ItemStackHandler decoded = CompositePackageItem.getContents(
                helper.getLevel().registryAccess(), composite);
        helper.assertValueEqual(decoded.getStackInSlot(0).getCount(), 12, "decoded iron count");
        helper.assertValueEqual(decoded.getStackInSlot(1).getCount(), 3, "decoded gold count");
        helper.assertValueEqual(CompositePackageItem.getChildren(
                helper.getLevel().registryAccess(), composite).size(), 1, "child count");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "composite_packages")
    public static void contentlessCompositeUsesChildStyle(GameTestHelper helper) {
        ItemStack child = PackageStyles.getDefaultBox();
        ItemStack composite = CompositePackageItem.of(helper.getLevel().registryAccess(),
                PackageItem.containing(List.of()), List.of(child));
        helper.assertFalse(CompositePackageItem.hasContent(composite), "composite has direct contents");
        helper.assertTrue(ItemStack.isSameItemSameComponents(
                CompositePackageItem.getEffectiveBox(helper.getLevel().registryAccess(), composite), child),
                "effective child style changed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "composite_packages")
    public static void flatteningNestedCompositePreservesItems(GameTestHelper helper) {
        ItemStack nestedParent = PackageItem.containing(List.of(new ItemStack(Items.DIAMOND, 5)));
        ItemStack nestedComposite = CompositePackageItem.of(helper.getLevel().registryAccess(),
                nestedParent, List.of(PackageStyles.getDefaultBox()));
        ItemStack flattened = CompositePackageItem.of(helper.getLevel().registryAccess(),
                PackageItem.containing(List.of(new ItemStack(Items.GOLD_INGOT, 2))),
                List.of(nestedComposite));
        ItemStackHandler contents = CompositePackageItem.getContents(
                helper.getLevel().registryAccess(), flattened);
        helper.assertValueEqual(count(contents, Items.DIAMOND), 5, "nested diamonds");
        helper.assertValueEqual(count(contents, Items.GOLD_INGOT), 2, "outer gold");
        helper.succeed();
    }

    private static int count(ItemStackHandler handler, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++)
            if (handler.getStackInSlot(slot).is(item))
                total += handler.getStackInSlot(slot).getCount();
        return total;
    }

    private CompositePackageGameTests() {
    }
}
