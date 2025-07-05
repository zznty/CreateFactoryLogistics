package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

public class ChemicalTankManipulationBehaviour extends CapManipulationBehaviourBase<IChemicalHandler, ChemicalTankManipulationBehaviour> {
    public static final BehaviourType<ChemicalTankManipulationBehaviour> TYPE = new BehaviourType<>();

    public ChemicalTankManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target) {
        super(be, target);
    }

    @Override
    protected BlockCapability<IChemicalHandler, Direction> capability() {
        return Capabilities.CHEMICAL.block();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
