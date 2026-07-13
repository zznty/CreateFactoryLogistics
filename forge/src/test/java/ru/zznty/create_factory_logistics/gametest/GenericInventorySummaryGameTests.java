package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class GenericInventorySummaryGameTests {
    @GameTest(template = "empty", batch = "generic_inventory_summary")
    public static void storesItemsAndFluids(GameTestHelper helper) {
        GenericStack diamonds = GenericStack.wrap(new ItemStack(Items.DIAMOND, 7));
        GenericStack water = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1500));
        GenericInventorySummary summary = GenericInventorySummary.empty();
        summary.add(diamonds);
        summary.add(water);

        helper.assertValueEqual(summary.getCountOf(diamonds.key()), 7, "diamond count");
        helper.assertValueEqual(summary.getCountOf(water.key()), 1500, "water count");
        helper.assertValueEqual(summary.get().size(), 2, "generic entry count");
        helper.assertFalse(summary.isEmpty(), "summary is empty");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_inventory_summary")
    public static void combinesEqualKeysAndSeparatesComponents(GameTestHelper helper) {
        ItemStack named = new ItemStack(Items.DIAMOND, 3);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("reserved"));
        GenericStack plain = GenericStack.wrap(new ItemStack(Items.DIAMOND, 4));
        GenericStack namedStack = GenericStack.wrap(named);
        GenericInventorySummary summary = GenericInventorySummary.empty();

        summary.add(plain);
        summary.add(namedStack);
        summary.add(namedStack.withAmount(5));

        helper.assertValueEqual(summary.getCountOf(plain.key()), 4, "plain diamond count");
        helper.assertValueEqual(summary.getCountOf(namedStack.key()), 8, "named diamond count");
        helper.assertValueEqual(summary.get().size(), 2, "component variant count");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_inventory_summary")
    public static void copyAndMergePreserveGenericEntries(GameTestHelper helper) {
        GenericStack item = GenericStack.wrap(new ItemStack(Items.COPPER_INGOT, 11));
        GenericStack fluid = FluidGenericStack.wrap(new FluidStack(Fluids.LAVA, 750));
        GenericInventorySummary left = GenericInventorySummary.empty();
        GenericInventorySummary right = GenericInventorySummary.empty();
        left.add(item);
        right.add(fluid);
        left.add(right);

        InventorySummary copy = left.asSummary().copy();
        GenericInventorySummary copied = GenericInventorySummary.of(copy);
        helper.assertValueEqual(copied.getCountOf(item.key()), 11, "copied item count");
        helper.assertValueEqual(copied.getCountOf(fluid.key()), 750, "copied fluid count");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_inventory_summary")
    public static void eraseRemovesOnlyMatchingVariant(GameTestHelper helper) {
        ItemStack named = new ItemStack(Items.DIAMOND, 6);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        GenericStack plain = GenericStack.wrap(new ItemStack(Items.DIAMOND, 4));
        GenericStack namedStack = GenericStack.wrap(named);
        GenericInventorySummary summary = GenericInventorySummary.empty();
        summary.add(plain);
        summary.add(namedStack);

        helper.assertTrue(summary.erase(namedStack.key()), "named entry was not erased");
        helper.assertValueEqual(summary.getCountOf(namedStack.key()), 0, "named count after erase");
        helper.assertValueEqual(summary.getCountOf(plain.key()), 4, "plain count after erase");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_inventory_summary")
    public static void removingExactAmountRemovesEntry(GameTestHelper helper) {
        GenericStack water = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1000));
        GenericInventorySummary summary = GenericInventorySummary.empty();
        summary.add(water);
        summary.add(water.withAmount(-1000));

        helper.assertValueEqual(summary.getCountOf(water.key()), 0, "water count");
        helper.assertTrue(summary.get().isEmpty(), "zero-sized entry remains");
        helper.assertTrue(summary.isEmpty(), "zero-sized entry keeps summary nonempty");
        helper.succeed();
    }

    private GenericInventorySummaryGameTests() {
    }
}
