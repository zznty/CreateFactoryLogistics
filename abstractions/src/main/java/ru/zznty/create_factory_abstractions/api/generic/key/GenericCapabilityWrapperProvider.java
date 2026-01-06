package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;

public interface GenericCapabilityWrapperProvider<Cap, ItemCap> {
    BlockCapability<Cap, Direction> capability();

    ItemCapability<ItemCap, Void> capabilityItem();

    Cap wrap(GenericInventorySummaryProvider summaryProvider, HolderLookup.Provider registries);

    GenericInventorySummaryProvider unwrap(Cap capability);
}
