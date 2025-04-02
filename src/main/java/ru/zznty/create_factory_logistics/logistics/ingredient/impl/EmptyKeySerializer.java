package ru.zznty.create_factory_logistics.logistics.ingredient.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class EmptyKeySerializer implements IngredientKeySerializer<EmptyIngredientKey> {
    @Override
    public void write(EmptyIngredientKey key, CompoundTag tag) {
    }

    @Override
    public void write(EmptyIngredientKey key, FriendlyByteBuf buf) {
    }

    @Override
    public EmptyIngredientKey read(CompoundTag tag) {
        return (EmptyIngredientKey) IngredientKey.of();
    }

    @Override
    public EmptyIngredientKey read(FriendlyByteBuf buf) {
        return (EmptyIngredientKey) IngredientKey.of();
    }
}
