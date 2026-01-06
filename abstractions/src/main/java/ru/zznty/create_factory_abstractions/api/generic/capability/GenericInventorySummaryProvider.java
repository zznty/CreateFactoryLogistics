package ru.zznty.create_factory_abstractions.api.generic.capability;

import net.minecraft.core.HolderLookup;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

@FunctionalInterface
public interface GenericInventorySummaryProvider {
    void apply(GenericInventorySummary summary, HolderLookup.Provider registries);
}
