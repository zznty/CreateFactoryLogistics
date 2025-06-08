package ru.zznty.create_factory_logistics.ponder;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.FactoryBlocks;

import java.util.List;

public class Scenes {
    public static final String
            MIXER_UPKEEP = "mixer_upkeep";

    public static void mixerUpkeep(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title(MIXER_UPKEEP, "Mixer Upkeep with Network links");
        scene.configureBasePlate(0, 0, 8);
        scene.scaleSceneView(.68f);
        scene.world().setKineticSpeed(util.select().everywhere(), 32f);
        scene.showBasePlate();
        scene.idle(5);

        BlockPos switchPos = util.grid().at(5, 1, 1);

        scene.world().showSection(util.select().position(switchPos), Direction.DOWN);

        Selection redstoneStuff = util.select().fromTo(1, 1, 0, 4, 1, 1);

        scene.world().toggleRedstonePower(redstoneStuff);

        scene.overlay().showControls(util.vector().topOf(switchPos), Pointing.DOWN, 30).whileSneaking().rightClick()
                .withItem(FactoryBlocks.NETWORK_LINK.asStack());

        scene.overlay().showText(50)
                .text("Network Links could be attached to a inventory reading device like Threshold Switch.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(switchPos));

        scene.idle(30);

        scene.world().showSection(util.select().fromTo(5, 1, 0, 5, 1, 0), Direction.DOWN);

        scene.idle(30);

        BlockPos armPos = util.grid().at(2, 1, 2);

        scene.overlay().showText(50)
                .text("So that blaze burner would be fed only when necessary.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(armPos));
        // arm
        scene.world().showSection(util.select().position(armPos), Direction.DOWN);

        scene.idle(20);

        // redstone and depot
        scene.world().showSection(util.select().fromTo(1, 1, 0, 4, 1, 1), Direction.DOWN);

        scene.idle(10);

        // mixer
        scene.world().showSection(util.select().fromTo(4, 1, 2, 4, 5, 2), Direction.DOWN);

        scene.world().multiplyKineticSpeed(util.select().position(4, 4, 2), 4);

        scene.idle(20);

        // belts
        scene.world().showSection(util.select().fromTo(5, 1, 2, 7, 2, 2), Direction.SOUTH);

        scene.idle(5);

        // pipes
        scene.world().showSection(util.select().fromTo(4, 1, 3, 5, 5, 7), Direction.NORTH);

        scene.idle(10);

        scene.overlay().showText(50)
                .text("When order has arrived, the switch will read new state and react.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(4, 4, 2));

        for (int i = 5; i < 8; i++) {
            scene.world().createItemOnBelt(util.grid().at(i, 1, 2), Direction.DOWN, Items.COBBLESTONE.getDefaultInstance());
        }

        scene.world().modifyBlockEntity(util.grid().at(4, 2, 2), BasinBlockEntity.class, be ->
                be.inputInventory.insertItem(0, Items.COBBLESTONE.getDefaultInstance().copyWithCount(16), false));

        scene.world().toggleRedstonePower(redstoneStuff);

        scene.world().instructArm(armPos, ArmBlockEntity.Phase.MOVE_TO_OUTPUT,
                Items.OAK_LOG.getDefaultInstance(), 0);

        scene.idle(40);

        scene.world().instructArm(armPos, ArmBlockEntity.Phase.SEARCH_INPUTS,
                ItemStack.EMPTY, 0);

        scene.world().modifyBlock(util.grid().at(4, 1, 2),
                state -> state.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), true);

        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(4, 4, 2), MechanicalMixerBlockEntity.class, MechanicalMixerBlockEntity::startProcessingBasin);

        scene.idle(30);

        scene.world().modifyBlockEntity(util.grid().at(4, 2, 2), BasinBlockEntity.class, be ->
                be.acceptOutputs(List.of(), List.of(new FluidStack(Fluids.LAVA.getSource(), 2000)), false));

        scene.idle(120);
    }
}
