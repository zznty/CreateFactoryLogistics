package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Special cases Create block entities to only pick their outputs instead of default
 */
public class OutputOnlyTankManipulationBehaviour extends TankManipulationBehaviour {
    public static final BehaviourType<OutputOnlyTankManipulationBehaviour> OBSERVE = new BehaviourType<>();

    public OutputOnlyTankManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
        super(be, target);
    }

    @Override
    public BehaviourType<?> getType() {
        return OBSERVE;
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
        if (!filter.test(invBE))
            return;
        if (invBE instanceof SmartBlockEntity be) {
            SmartFluidTankBehaviour tankBehaviour = be.getBehaviour(SmartFluidTankBehaviour.OUTPUT);
            if (tankBehaviour != null) {
                targetCapability = tankBehaviour.getCapability().cast();
                return;
            }
        }

        super.findNewCapability();
    }
}
