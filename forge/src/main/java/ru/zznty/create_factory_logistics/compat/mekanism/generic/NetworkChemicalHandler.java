package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.List;

public class NetworkChemicalHandler implements IChemicalHandler {
    private final List<GenericStack> stacks;

    public NetworkChemicalHandler(GenericInventorySummaryProvider summaryProvider) {
        GenericInventorySummary summary = GenericInventorySummary.empty();
        summaryProvider.apply(summary);
        stacks = summary.get().stream().filter(s -> s.key() instanceof ChemicalKey).toList();
    }

    @Override
    public int getChemicalTanks() {
        return stacks.size();
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return asChemical(stacks.get(tank));
    }

    @Override
    public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return stacks.get(tank).amount();
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        return asChemical(stacks.get(tank)).is(stack.getChemicalHolder());
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        return stack;
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        return ChemicalStack.EMPTY;
    }

    private static ChemicalStack asChemical(GenericStack stack) {
        if (stack.key() instanceof ChemicalKey(net.minecraft.core.Holder<mekanism.api.chemical.Chemical> chemical))
            return new ChemicalStack(chemical, stack.amount());
        return ChemicalStack.EMPTY;
    }
}
