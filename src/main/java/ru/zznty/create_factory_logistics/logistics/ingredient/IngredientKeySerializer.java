package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Provides serialization logic for the specific implementation of IngredientKey
 * Type matching is handled by the registry
 */
public interface IngredientKeySerializer<Key extends IngredientKey> {
    void write(Key key, CompoundTag tag);

    void write(Key key, FriendlyByteBuf buf);

    Key read(CompoundTag tag);

    Key read(FriendlyByteBuf buf);
}
