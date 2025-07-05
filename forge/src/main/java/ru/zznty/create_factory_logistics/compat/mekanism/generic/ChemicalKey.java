package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.Holder;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

public record ChemicalKey(Holder<Chemical> chemical) implements GenericKey {
    public ChemicalStack stack() {
        return new ChemicalStack(chemical, 1);
    }
}
