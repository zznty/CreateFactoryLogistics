package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;

public class GeneticInventoryBehaviour extends CapManipulationBehaviourBase<GenericInventory, GeneticInventoryBehaviour> {
    public static final BehaviourType<GeneticInventoryBehaviour> TYPE = new BehaviourType<>();

    public GeneticInventoryBehaviour(SmartBlockEntity be,
                                     InterfaceProvider target) {
        super(be, target);
    }

    @Override
    protected Capability<GenericInventory> capability() {
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

        targetCapability = LazyOptional.empty();

        if (!world.isLoaded(pos))
            return;
        BlockEntity invBE = world.getBlockEntity(pos);
        if (invBE == null || !filter.test(invBE))
            return;
        targetCapability = LazyOptional.of(() -> GenericInventory.of(invBE));
    }
}
