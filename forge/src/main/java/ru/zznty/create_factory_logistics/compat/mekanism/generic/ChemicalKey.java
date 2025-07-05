package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

public record ChemicalKey(Chemical<?> chemical) implements GenericKey {
    public ChemicalStack<?> stack() {
        return chemical.getStack(1);
    }
}
