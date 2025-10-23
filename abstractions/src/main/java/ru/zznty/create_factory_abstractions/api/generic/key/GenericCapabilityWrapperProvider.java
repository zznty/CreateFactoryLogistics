package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;

public interface GenericCapabilityWrapperProvider<Cap> {
    BlockCapability<Cap, Direction> capability();

    Cap wrap(GenericInventorySummaryProvider summaryProvider);

    GenericInventorySummaryProvider unwrap(Cap capability);
}
