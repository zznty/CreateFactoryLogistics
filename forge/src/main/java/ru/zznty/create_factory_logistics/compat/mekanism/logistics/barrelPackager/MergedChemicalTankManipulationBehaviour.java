package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import com.google.common.base.Predicate;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MergedChemicalTankManipulationBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<MergedChemicalTankManipulationBehaviour> TYPE = new BehaviourType<>();
    private final List<ChemicalTankManipulationBehaviour> behaviours;

    @Nullable
    private ChemicalTankManipulationBehaviour lastActiveBehaviour;

    public MergedChemicalTankManipulationBehaviour(SmartBlockEntity be,
                                                   List<ChemicalTankManipulationBehaviour> behaviours) {
        super(be);
        this.behaviours = behaviours;
        setLazyTickRate(5);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean hasInventory() {
        return getInventory() != null;
    }

    public IChemicalHandler<?, ?> getInventory() {
        if (lastActiveBehaviour != null && lastActiveBehaviour.hasInventory())
            return lastActiveBehaviour.getInventory();

        return null;
    }

    public ChemicalTankManipulationBehaviour getActive() {
        if (lastActiveBehaviour != null && lastActiveBehaviour.hasInventory())
            return lastActiveBehaviour;

        return null;
    }

    @Override
    public void lazyTick() {
        if (lastActiveBehaviour == null)
            findNewActive();
    }

    public void findNewActive() {
        for (ChemicalTankManipulationBehaviour behaviour : behaviours) {
            behaviour.findNewCapability();
            if (behaviour.hasInventory()) {
                lastActiveBehaviour = behaviour;
                break;
            }
        }
    }

    public MergedChemicalTankManipulationBehaviour withFilter(Predicate<BlockEntity> filter) {
        for (ChemicalTankManipulationBehaviour behaviour : behaviours) {
            behaviour.withFilter(filter);
        }
        return this;
    }
}
