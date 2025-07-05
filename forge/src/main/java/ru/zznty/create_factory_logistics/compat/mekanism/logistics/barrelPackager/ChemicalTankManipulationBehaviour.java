package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraftforge.common.capabilities.Capability;

import java.util.List;

public class ChemicalTankManipulationBehaviour extends CapManipulationBehaviourBase<IChemicalHandler<?, ?>, ChemicalTankManipulationBehaviour> {
    public static final BehaviourType<ChemicalTankManipulationBehaviour> GAS = new BehaviourType<>("gas");
    public static final BehaviourType<ChemicalTankManipulationBehaviour> INFUSION_TYPE = new BehaviourType<>(
            "infusion_type");
    public static final BehaviourType<ChemicalTankManipulationBehaviour> PIGMENT = new BehaviourType<>("pigment");
    public static final BehaviourType<ChemicalTankManipulationBehaviour> SLURRY = new BehaviourType<>("slurry");

    private final Capability<IChemicalHandler<?, ?>> capability;
    private final BehaviourType<ChemicalTankManipulationBehaviour> behaviourType;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ChemicalTankManipulationBehaviour(SmartBlockEntity be, InterfaceProvider target,
                                             Capability capability,
                                             BehaviourType<ChemicalTankManipulationBehaviour> behaviourType) {
        super(be, target);
        this.capability = capability;
        this.behaviourType = behaviourType;
    }

    @Override
    protected Capability<IChemicalHandler<?, ?>> capability() {
        return capability;
    }

    @Override
    public BehaviourType<?> getType() {
        return behaviourType;
    }

    public static ChemicalTankManipulationBehaviour forGas(SmartBlockEntity be, InterfaceProvider target) {
        return new ChemicalTankManipulationBehaviour(be, target, Capabilities.GAS_HANDLER, GAS);
    }

    public static ChemicalTankManipulationBehaviour forInfusionType(SmartBlockEntity be, InterfaceProvider target) {
        return new ChemicalTankManipulationBehaviour(be, target, Capabilities.INFUSION_HANDLER, INFUSION_TYPE);
    }

    public static ChemicalTankManipulationBehaviour forPigment(SmartBlockEntity be, InterfaceProvider target) {
        return new ChemicalTankManipulationBehaviour(be, target, Capabilities.PIGMENT_HANDLER, PIGMENT);
    }

    public static ChemicalTankManipulationBehaviour forSlurry(SmartBlockEntity be, InterfaceProvider target) {
        return new ChemicalTankManipulationBehaviour(be, target, Capabilities.SLURRY_HANDLER, SLURRY);
    }

    public static MergedChemicalTankManipulationBehaviour forAll(SmartBlockEntity be, InterfaceProvider target,
                                                                 List<BlockEntityBehaviour> behaviours) {
        List<ChemicalTankManipulationBehaviour> caps = List.of(forGas(be, target), forInfusionType(be, target),
                                                               forPigment(be, target), forSlurry(be, target));
        behaviours.addAll(caps);
        return new MergedChemicalTankManipulationBehaviour(be, caps);
    }
}
