package ru.zznty.create_factory_logistics.ponder;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.FrogAndConveyorScenes;
import com.simibubi.create.infrastructure.ponder.scenes.highLogistics.PonderHilo;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
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
            MIXER_UPKEEP = "mixer_upkeep",
            AE_INTERFACE = "ae_interface";

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

    /**
     * Demonstrates the AE2 integration: an AE2 ME Interface with a Stock Link on its
     * side and a Frogport on top behaves like a packager for a Create logistics network.
     * The structure ({@code ae_interface.nbt}) is derived from Create-Stock-Bridge's scene
     * with the custom bridge block swapped for a vanilla {@code ae2:interface} + stock link.
     */
    public static void aeInterface(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        BlockPos iface = util.grid().at(3, 1, 2);
        BlockPos frogport = util.grid().at(3, 2, 2);
        BlockPos link = util.grid().at(2, 1, 2);
        BlockPos ticker = util.grid().at(5, 1, 1);
        BlockPos conv1 = util.grid().at(5, 4, 1);
        BlockPos conv2 = util.grid().at(5, 4, 6);

        scene.title(AE_INTERFACE, "Bridging Applied Energistics with the Stock System");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(.75f);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();
        scene.idle(5);

        // The interface itself
        ElementLink<WorldSectionElement> ifaceSection =
                scene.world().showIndependentSection(util.select().position(iface), Direction.DOWN);
        scene.world().moveSection(ifaceSection, util.vector().of(0, 0, 0), 0);
        scene.idle(10);

        scene.overlay().showText(80)
                .pointAt(util.vector().topOf(iface))
                .placeNearTarget()
                .attachKeyFrame()
                .text("An AE2 ME Interface can act as a packager for a Create logistics network");
        scene.idle(85);

        // The ME network it belongs to (controller + energy cell sitting right behind it)
        ElementLink<WorldSectionElement> ae =
                scene.world().showIndependentSection(util.select().fromTo(3, 1, 3, 3, 1, 4), Direction.DOWN);
        scene.world().moveSection(ae, util.vector().of(0, 0, 0), 0);
        scene.idle(10);

        scene.overlay()
                .showText(50)
                .colored(PonderPalette.BLUE)
                .text("ME")
                .pointAt(util.vector().topOf(util.grid().at(3, 1, 3)))
                .placeNearTarget();
        scene.idle(20);

        // Stock link on the interface's west face
        ElementLink<WorldSectionElement> linkSection =
                scene.world().showIndependentSection(util.select().position(link), Direction.DOWN);
        scene.world().moveSection(linkSection, util.vector().of(0, 0, 0), 0);
        scene.idle(10);

        scene.overlay().showText(80)
                .pointAt(util.vector().centerOf(link))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Attach a Stock Link to bind the interface to a logistics network");
        scene.idle(85);

        // Frogport on top + the chain network (ticker & conveyors) it delivers through
        ElementLink<WorldSectionElement> delivery =
                scene.world().showIndependentSection(util.select().position(frogport)
                        .add(util.select().fromTo(5, 1, 1, 5, 4, 6)), Direction.DOWN);
        scene.world().moveSection(delivery, util.vector().of(0, 0, 0), 0);
        scene.idle(10);

        scene.overlay().showText(80)
                .pointAt(util.vector().topOf(frogport))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Place a Frogport on top to send and receive packages");
        scene.idle(85);

        // Direction 1: the network requests items stored in AE, dispatched through the Frogport.
        // The Frogport is bound to the near chain conveyor, so startAnimation(deposit=true)
        // places the built package onto the chain itself - no separate, desyncable package.
        scene.overlay().showText(80)
                .pointAt(util.vector().topOf(ticker))
                .placeNearTarget()
                .attachKeyFrame()
                .text("The network can request items stored in AE");
        scene.idle(10);
        PonderHilo.linkEffect(scene, ticker);
        PonderHilo.requesterEffect(scene, link);
        scene.idle(15);

        ItemStack outgoing = PackageStyles.getDefaultBox().copy();
        PackageItem.addAddress(outgoing, "Vault");
        PonderHilo.requesterEffect(scene, iface);
        scene.world().modifyBlockEntity(frogport, FrogportBlockEntity.class,
                be -> be.startAnimation(outgoing, true));
        scene.idle(95);
        // Clear the package once it has travelled off (address has no matching frog here)
        scene.world().modifyBlockEntity(conv2, ChainConveyorBlockEntity.class,
                be -> be.getLoopingPackages().clear());
        scene.world().modifyBlockEntity(conv1, ChainConveyorBlockEntity.class,
                be -> be.getLoopingPackages().clear());
        scene.idle(10);

        // Direction 2: a package addressed to this Frogport travels in on the chain and the
        // frog catches it via the game's own routing (perfectly in sync), then it is unpacked.
        scene.overlay().showText(80)
                .pointAt(util.vector().topOf(frogport))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Packages delivered here are unpacked into the ME network");
        scene.idle(10);

        ItemStack incoming = PackageStyles.getDefaultBox().copy();
        PackageItem.addAddress(incoming, "AE"); // matches the Frogport's address, so it routes here
        scene.world().modifyBlockEntity(conv1, ChainConveyorBlockEntity.class, be -> {
            be.addLoopingPackage(new ChainConveyorPackage(0, incoming));
            FrogAndConveyorScenes.boxTransfer(conv2, conv1, be);
        });
        scene.idle(90);
        PonderHilo.requesterEffect(scene, iface);
        PonderHilo.linkEffect(scene, ticker);
        scene.idle(30);
    }
}
