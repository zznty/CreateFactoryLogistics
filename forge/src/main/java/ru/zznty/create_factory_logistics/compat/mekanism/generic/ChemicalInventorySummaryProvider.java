package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

public class ChemicalInventorySummaryProvider implements GenericInventorySummaryProvider {
    private final IChemicalHandler capability;

    public ChemicalInventorySummaryProvider(IChemicalHandler capability) {
        this.capability = capability;
    }

    @Override
    public void apply(GenericInventorySummary summary) {
        for (int i = 0; i < capability.getChemicalTanks(); i++) {
            ChemicalStack stack = capability.getChemicalInTank(i);
            if (stack.isEmpty()) continue;
            summary.add(ChemicalGenericStack.wrap(stack));
        }
    }
}
