package ru.zznty.create_factory_logistics.gametest;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.config.WorldConfig;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackageBuilder;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkMode;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class PackagingGameTests {
    @GameTest(template = "empty", batch = "packaging")
    public static void networkLinkModesSelectExpectedStock(GameTestHelper helper) {
        helper.assertTrue(NetworkLinkMode.STORED.includesStored(), "stored mode excludes stored stock");
        helper.assertFalse(NetworkLinkMode.STORED.includesPromised(), "stored mode includes promises");
        helper.assertFalse(NetworkLinkMode.PROMISED.includesStored(), "promised mode includes stored stock");
        helper.assertTrue(NetworkLinkMode.PROMISED.includesPromised(), "promised mode excludes promises");
        helper.assertTrue(NetworkLinkMode.ALL.includesStored(), "all mode excludes stored stock");
        helper.assertTrue(NetworkLinkMode.ALL.includesPromised(), "all mode excludes promises");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "packaging")
    public static void jarBuilderCapsContentsAndBuildsJar(GameTestHelper helper) {
        int capacity = WorldConfig.jarCapacity;
        FluidStack source = new FluidStack(Fluids.WATER, capacity + 250);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("batch water"));
        GenericStack requested = FluidGenericStack.wrap(source);
        JarPackageBuilder builder = new JarPackageBuilder();

        helper.assertValueEqual(builder.slotCount(), 1, "jar builder slot count");
        helper.assertValueEqual(builder.maxPerSlot(), capacity, "jar builder capacity");
        helper.assertValueEqual(builder.measure(requested.key()), PackageMeasureResult.BULKY,
                "fluid package measurement");
        helper.assertValueEqual(builder.add(requested), 250, "jar overflow remainder");
        helper.assertTrue(builder.isFull(), "jar builder is not full");

        ItemStack jar = builder.build();
        helper.assertTrue(jar.getItem() instanceof JarPackageItem, "builder did not produce a jar");
        FluidStack contained = FluidUtil.getFluidContained(jar).orElse(FluidStack.EMPTY);
        helper.assertValueEqual(contained.getAmount(), capacity, "built jar amount");
        helper.assertTrue(FluidStack.isSameFluidSameComponents(contained, source),
                "built jar changed fluid components");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "packaging")
    public static void jarBuilderRejectsMismatchedFluid(GameTestHelper helper) {
        JarPackageBuilder builder = new JarPackageBuilder();
        int amount = Math.max(1, WorldConfig.jarCapacity / 2);
        GenericStack water = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, amount));
        GenericStack lava = FluidGenericStack.wrap(new FluidStack(Fluids.LAVA, 1));

        helper.assertValueEqual(builder.add(water), 0, "water add remainder");
        helper.assertValueEqual(builder.add(lava), -1, "mismatched fluid result");
        helper.assertValueEqual(builder.content().getFirst(), water, "rejected fluid mutated builder");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "packaging")
    public static void emptyJarBuilderProducesNoItem(GameTestHelper helper) {
        JarPackageBuilder builder = new JarPackageBuilder();
        helper.assertTrue(builder.content().getFirst().isEmpty(), "new builder has contents");
        helper.assertFalse(builder.isFull(), "new builder is full");
        helper.assertTrue(builder.build().isEmpty(), "empty builder produced an item");
        helper.succeed();
    }

    private PackagingGameTests() {
    }
}
