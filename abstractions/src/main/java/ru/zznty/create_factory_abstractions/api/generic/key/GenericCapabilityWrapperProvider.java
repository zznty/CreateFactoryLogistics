package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraftforge.common.capabilities.Capability;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;

public interface GenericCapabilityWrapperProvider<Cap> {
    Capability<Cap> capability();

    Cap wrap(GenericInventorySummaryProvider summaryProvider);

    GenericInventorySummaryProvider unwrap(Cap capability);
}
