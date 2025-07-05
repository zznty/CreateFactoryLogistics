package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;
import ru.zznty.create_factory_logistics.config.WorldConfig;
import ru.zznty.create_factory_logistics.logistics.abstractions.packager.AbstractPackagerBlockEntity;

import java.util.List;

public class JarPackagerBlockEntity extends AbstractPackagerBlockEntity {
    public TankManipulationBehaviour drainInventory;

    public JarPackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected PackagerAttachedHandler createHandler() {
        return new JarPackagerAttachedHandler(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        if (WorldConfig.jarPackagerPrefersOutputs) {
            drainInventory = new OutputOnlyTankManipulationBehaviour(this,
                                                                     CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing());
        } else {
            drainInventory = new TankManipulationBehaviour(this,
                                                           CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing());
        }

        behaviours.add(drainInventory.withFilter(this::supportsBlockEntity));
    }

    @Override
    protected boolean targetsSameInventory(GenericIdentifiedInventory inv) {
        if (!drainInventory.hasInventory())
            return false;

        if (inv.handler() == drainInventory.getInventory())
            return true;

        if (inv.identifier() == null)
            return false;

        BlockFace face = drainInventory.getTarget().getOpposite();
        return inv.identifier().contains(face);
    }
}
