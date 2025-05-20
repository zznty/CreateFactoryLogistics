package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Provides serialization logic for the specific implementation of IngredientKey
 * Type matching is handled by the registry
 */
public interface IngredientKeySerializer<Key extends IngredientKey> {
    void write(HolderLookup.Provider levelRegistryAccess, Key key, CompoundTag tag);

    void write(Key key, RegistryFriendlyByteBuf buf);

    Key read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag);

    Key read(RegistryFriendlyByteBuf buf);
}
