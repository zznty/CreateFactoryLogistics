package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;

public class GeneticInventoryBehaviour extends CapManipulationBehaviourBase<GenericInventory, GeneticInventoryBehaviour> {
    public static final BehaviourType<GeneticInventoryBehaviour> TYPE = new BehaviourType<>();

    public GeneticInventoryBehaviour(SmartBlockEntity be,
                                     InterfaceProvider target) {
        super(be, target);
    }

    @Override
    protected BlockCapability<GenericInventory, Direction> capability() {
        return AbstractionsCapabilities.GENERIC_INVENTORY;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void findNewCapability() {
        Level world = getWorld();
        BlockFace targetBlockFace = this.getTarget().getOpposite();
        BlockPos pos = targetBlockFace.getPos();

        targetCapability = null;

        if (!world.isLoaded(pos))
            return;
        BlockEntity invBE = world.getBlockEntity(pos);
        if (invBE == null || !filter.test(invBE))
            return;
        targetCapability = GenericInventory.of(world, pos);
    }
}
