package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;
import ru.zznty.create_factory_logistics.logistics.abstractions.packager.AbstractPackagerBlockEntity;

import java.util.List;

public class BarrelPackagerBlockEntity extends AbstractPackagerBlockEntity {
    public MergedChemicalTankManipulationBehaviour drainInventory;

    public BarrelPackagerBlockEntity(BlockEntityType<?> typeIn,
                                     BlockPos pos,
                                     BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        behaviours.add(drainInventory = ChemicalTankManipulationBehaviour.forAll(this,
                                                                                 CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing(),
                                                                                 behaviours)
                .withFilter(this::supportsBlockEntity));
    }

    @Override
    protected PackagerAttachedHandler createHandler() {
        return new BarrelPackagerAttachedHandler(this);
    }

    @Override
    protected boolean targetsSameInventory(GenericIdentifiedInventory inv) {
        if (!drainInventory.hasInventory())
            return false;

        if (inv.handler() == drainInventory.getInventory())
            return true;

        if (inv.identifier() == null)
            return false;

        BlockFace face = CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing()
                .getTarget(level, worldPosition, getBlockState()).getOpposite();
        return inv.identifier().contains(face);
    }
}
