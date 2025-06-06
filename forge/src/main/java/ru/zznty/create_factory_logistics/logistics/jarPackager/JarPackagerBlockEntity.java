package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;
import ru.zznty.create_factory_logistics.Config;

import java.util.List;

public class JarPackagerBlockEntity extends PackagerBlockEntity {
    public TankManipulationBehaviour drainInventory;

    public JarPackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        if (Config.jarPackagerPrefersOutputs) {
            drainInventory = new OutputOnlyTankManipulationBehaviour(this,
                                                                     CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing());
        } else {
            drainInventory = new TankManipulationBehaviour(this,
                                                           CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing());
        }

        behaviours.add(drainInventory.withFilter(this::supportsBlockEntity));
    }

    private boolean supportsBlockEntity(BlockEntity target) {
        return target != null && !(target instanceof PortableStorageInterfaceBlockEntity);
    }

    @Override
    public boolean isTargetingSameInventory(@Nullable IdentifiedInventory inventory) {
        if (inventory == null)
            return false;

        if (!drainInventory.hasInventory())
            return false;

        GenericIdentifiedInventory inv = GenericIdentifiedInventory.from(inventory);
        if (inv.handler() == drainInventory.getInventory())
            return true;

        if (inventory.identifier() == null)
            return false;

        BlockFace face = drainInventory.getTarget().getOpposite();
        return inventory.identifier().contains(face);
    }
}
