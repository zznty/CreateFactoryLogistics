package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import net.createmod.catnip.data.Pair;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericAttribute;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.generic.FluidNoNbtGenericAttribute;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackageBuilder;

import java.util.List;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FluidAttributeFilterGameTests {

    // --- FluidStack path (test via FluidStack) ---

    @GameTest(template = "empty", batch = "fluid_attribute_filter", timeoutTicks = 40)
    public static void fluidAttributeMatchesSameFluid(GameTestHelper helper) {
        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        FluidKey key = (FluidKey) FluidGenericStack.wrap(water).key();
        FluidGenericAttribute attr = new FluidGenericAttribute(key);

        FilterItemStack filter = attributeFilter(AttributeFilterWhitelistMode.WHITELIST_DISJ,
                Pair.of(attr, false));

        helper.assertTrue(filter.test(helper.getLevel(), water, true),
                "is_fluid attribute should match same fluid");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_attribute_filter", timeoutTicks = 40)
    public static void fluidAttributeRejectsDifferentFluid(GameTestHelper helper) {
        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        FluidStack lava = new FluidStack(Fluids.LAVA, 1000);

        FluidKey key = (FluidKey) FluidGenericStack.wrap(water).key();
        FluidGenericAttribute attr = new FluidGenericAttribute(key);

        FilterItemStack filter = attributeFilter(AttributeFilterWhitelistMode.WHITELIST_DISJ,
                Pair.of(attr, false));

        helper.assertTrue(filter.test(helper.getLevel(), water, true),
                "is_fluid should match water");
        helper.assertFalse(filter.test(helper.getLevel(), lava, true),
                "is_fluid should not match lava");
        helper.succeed();
    }

    // --- ItemStack path (test via jar ItemStack — what funnels/belts actually do) ---

    @GameTest(template = "empty", batch = "fluid_attribute_filter", timeoutTicks = 40)
    public static void fluidAttributeMatchesJarItemStack(GameTestHelper helper) {
        ItemStack jar = makeJar(Fluids.WATER, 1000);
        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        FluidKey key = (FluidKey) FluidGenericStack.wrap(water).key();
        FluidGenericAttribute attr = new FluidGenericAttribute(key);

        FilterItemStack filter = attributeFilter(AttributeFilterWhitelistMode.WHITELIST_DISJ,
                Pair.of(attr, false));

        helper.assertTrue(filter.test(helper.getLevel(), jar, true),
                "is_fluid attribute should match a jar ItemStack containing the same fluid (bug #224)");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_attribute_filter", timeoutTicks = 40)
    public static void fluidNoNbtAttributeMatchesJarItemStack(GameTestHelper helper) {
        ItemStack jar = makeJar(Fluids.WATER, 1000);
        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        FluidKey key = (FluidKey) FluidGenericStack.wrap(water).key();
        FluidNoNbtGenericAttribute attr = new FluidNoNbtGenericAttribute(key);

        FilterItemStack filter = attributeFilter(AttributeFilterWhitelistMode.WHITELIST_DISJ,
                Pair.of(attr, false));

        helper.assertTrue(filter.test(helper.getLevel(), jar, true),
                "is_fluid_no_nbt attribute should match a jar ItemStack containing the same fluid (bug #224)");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_attribute_filter", timeoutTicks = 40)
    public static void fluidAttributeRejectsJarWithDifferentFluid(GameTestHelper helper) {
        ItemStack waterJar = makeJar(Fluids.WATER, 1000);
        ItemStack lavaJar = makeJar(Fluids.LAVA, 1000);

        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        FluidKey key = (FluidKey) FluidGenericStack.wrap(water).key();
        FluidGenericAttribute attr = new FluidGenericAttribute(key);

        FilterItemStack filter = attributeFilter(AttributeFilterWhitelistMode.WHITELIST_DISJ,
                Pair.of(attr, false));

        helper.assertTrue(filter.test(helper.getLevel(), waterJar, true),
                "is_fluid should match water jar");
        helper.assertFalse(filter.test(helper.getLevel(), lavaJar, true),
                "is_fluid should not match lava jar");
        helper.succeed();
    }

    // --- helpers ---

    @SafeVarargs
    private static FilterItemStack attributeFilter(AttributeFilterWhitelistMode mode,
                                                    Pair<ItemAttribute, Boolean>... tests) {
        ItemStack filterItem = new ItemStack(AllItems.ATTRIBUTE_FILTER.get());
        filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, mode);
        List<ItemAttribute.ItemAttributeEntry> entries = new java.util.ArrayList<>();
        for (Pair<ItemAttribute, Boolean> test : tests)
            entries.add(new ItemAttribute.ItemAttributeEntry(test.getFirst(), test.getSecond()));
        filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, entries);
        return FilterItemStack.of(filterItem);
    }

    private static ItemStack makeJar(net.minecraft.world.level.material.Fluid fluid, int amount) {
        JarPackageBuilder builder = new JarPackageBuilder();
        builder.add(FluidGenericStack.wrap(new FluidStack(fluid, amount)));
        return builder.build();
    }

    private FluidAttributeFilterGameTests() {}
}
