package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlockEntity;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FactoryPanelSerializationGameTests {
    @GameTest(template = "empty", batch = "factory_panel_serialization")
    public static void saveLoadPreservesPanelConfiguration(GameTestHelper helper) {
        var source = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 0), 250);
        var target = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 2), 12000);
        FactoryPanelConnection connection = FactoryPanelTestSupport.connect(source, target, 750);
        connection.arrowBendMode = 3;
        target.behaviour().recipeAddress = "mixing";
        target.behaviour().recipeOutput = 2000;
        target.behaviour().promiseClearingInterval = 5;

        CompoundTag saved = target.blockEntity().saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loadedBase = BlockEntity.loadStatic(helper.absolutePos(target.position()),
                target.blockEntity().getBlockState(), saved, helper.getLevel().registryAccess());
        helper.assertTrue(loadedBase instanceof FactoryFluidPanelBlockEntity,
                "saved panel did not load");
        FactoryFluidPanelBehaviour loaded = (FactoryFluidPanelBehaviour)
                ((FactoryFluidPanelBlockEntity) loadedBase).panels.get(FactoryPanelTestSupport.SLOT);
        helper.assertTrue(loaded.active, "loaded panel inactive");
        helper.assertValueEqual(loaded.count, 12000, "loaded count");
        helper.assertValueEqual(loaded.recipeAddress, "mixing", "loaded address");
        helper.assertValueEqual(loaded.recipeOutput, 2000, "loaded recipe output");
        helper.assertValueEqual(loaded.promiseClearingInterval, 5, "loaded promise interval");
        helper.assertValueEqual(loaded.targetedBy.get(source.behaviour().getPanelPosition()).amount,
                750, "loaded connection amount");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "factory_panel_serialization")
    public static void zeroRecipeOutputLoadsAsOne(GameTestHelper helper) {
        var fixture = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 0), 1000);
        CompoundTag saved = fixture.blockEntity().saveWithFullMetadata(helper.getLevel().registryAccess());
        saved.getCompound(FactoryPanelTestSupport.SLOT.getSerializedName()).putInt("RecipeOutput", 0);
        FactoryFluidPanelBlockEntity loaded = (FactoryFluidPanelBlockEntity) BlockEntity.loadStatic(
                helper.absolutePos(fixture.position()), fixture.blockEntity().getBlockState(), saved,
                helper.getLevel().registryAccess());
        helper.assertTrue(loaded != null, "panel did not load");
        helper.assertValueEqual(loaded.panels.get(FactoryPanelTestSupport.SLOT).recipeOutput, 1,
                "zero recipe output migration");
        helper.succeed();
    }

    private FactoryPanelSerializationGameTests() {
    }
}
