package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FactoryPanelValueGameTests {
    @GameTest(template = "empty", batch = "factory_panel_values")
    public static void valueSettingsScaleBuckets(GameTestHelper helper) {
        var fixture = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 0), 0);
        fixture.behaviour().setValueSettings(helper.makeMockPlayer(GameType.CREATIVE),
                new ValueSettings(0, 750), false);
        helper.assertValueEqual(fixture.behaviour().count, 750, "millibucket count");
        fixture.behaviour().setValueSettings(helper.makeMockPlayer(GameType.CREATIVE),
                new ValueSettings(1, 12), false);
        helper.assertValueEqual(fixture.behaviour().count, 12000, "bucket count");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "factory_panel_values")
    public static void formatsFluidAmounts(GameTestHelper helper) {
        helper.assertValueEqual(FactoryFluidPanelBehaviour.formatLevel(0).component().getString(),
                "0B", "zero format");
        helper.assertValueEqual(FactoryFluidPanelBehaviour.formatLevel(999).component().getString(),
                "999mB", "millibucket format");
        helper.assertValueEqual(FactoryFluidPanelBehaviour.formatLevel(1000).component().getString(),
                "1B", "bucket format");
        helper.assertValueEqual(FactoryFluidPanelBehaviour.formatLevel(1500).component().getString(),
                "1,500mB", "fractional bucket format");
        helper.assertValueEqual(FactoryFluidPanelBehaviour.formatLevel(1_000_000).component().getString(),
                "1kB", "large bucket format");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "factory_panel_values")
    public static void filterExposesGenericFluidDemand(GameTestHelper helper) {
        var fixture = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 0), 2500);
        helper.assertValueEqual(fixture.behaviour().filter().amount(), 2500, "generic fluid demand");
        helper.assertFalse(fixture.behaviour().filter().isEmpty(), "generic fluid demand is empty");
        helper.succeed();
    }

    private FactoryPanelValueGameTests() {
    }
}
