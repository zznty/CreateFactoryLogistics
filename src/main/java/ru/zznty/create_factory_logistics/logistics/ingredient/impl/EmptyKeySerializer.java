package ru.zznty.create_factory_logistics.logistics.ingredient.impl;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class EmptyKeySerializer implements IngredientKeySerializer<EmptyIngredientKey> {
    @Override
    public void write(HolderLookup.Provider levelRegistryAccess, EmptyIngredientKey key, CompoundTag tag) {
    }

    @Override
    public void write(EmptyIngredientKey key, RegistryFriendlyByteBuf buf) {
    }

    @Override
    public EmptyIngredientKey read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        return (EmptyIngredientKey) IngredientKey.of();
    }

    @Override
    public EmptyIngredientKey read(RegistryFriendlyByteBuf buf) {
        return (EmptyIngredientKey) IngredientKey.of();
    }
}
