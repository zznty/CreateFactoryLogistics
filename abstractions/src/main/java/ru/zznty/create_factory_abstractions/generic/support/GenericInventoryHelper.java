package ru.zznty.create_factory_abstractions.generic.support;

import net.minecraft.core.HolderLookup;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

public class GenericInventoryHelper {
    public static void fillSummary(GenericInventory inventory, GenericInventorySummary summary, HolderLookup.Provider registries) {
        for (GenericKeyRegistration registration : GenericContentExtender.REGISTRATIONS.values()) {
            GenericInventorySummaryProvider provider = inventory.get(registration);
            if (provider == null) continue;
            provider.apply(summary, registries);
        }
    }
}
