package ru.zznty.create_factory_abstractions.api.generic.capability;

import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

@FunctionalInterface
public interface GenericInventorySummaryProvider {
    void apply(boolean scanInputSlots, GenericInventorySummary summary);
}
