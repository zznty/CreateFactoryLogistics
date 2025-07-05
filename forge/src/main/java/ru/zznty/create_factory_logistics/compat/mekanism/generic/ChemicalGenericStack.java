package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.chemical.ChemicalStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public class ChemicalGenericStack {
    public static GenericStack wrap(ChemicalStack<?> chemicalStack) {
        if (chemicalStack.getRaw().isEmptyType()) return GenericStack.EMPTY;
        return new GenericStack(new ChemicalKey(chemicalStack.getRaw()),
                                (int) Math.min(chemicalStack.getAmount(), Integer.MAX_VALUE));
    }
}
