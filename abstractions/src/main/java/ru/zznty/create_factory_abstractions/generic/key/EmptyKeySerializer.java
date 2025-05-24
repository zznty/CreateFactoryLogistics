package ru.zznty.create_factory_abstractions.generic.key;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

public class EmptyKeySerializer implements GenericKeySerializer<EmptyKey> {
    @Override
    public EmptyKey read(CompoundTag tag) {
        return (EmptyKey) GenericKey.EMPTY;
    }

    @Override
    public void write(EmptyKey key, CompoundTag tag) {
    }

    @Override
    public EmptyKey read(FriendlyByteBuf buf) {
        return (EmptyKey) GenericKey.EMPTY;
    }

    @Override
    public void write(EmptyKey key, FriendlyByteBuf buf) {
    }
}
