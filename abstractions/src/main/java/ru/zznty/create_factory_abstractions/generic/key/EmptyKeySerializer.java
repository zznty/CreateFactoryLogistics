package ru.zznty.create_factory_abstractions.generic.key;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

public class EmptyKeySerializer implements GenericKeySerializer<EmptyKey> {
    @Override
    public EmptyKey read(HolderLookup.Provider registries, CompoundTag tag) {
        return (EmptyKey) GenericKey.EMPTY;
    }

    @Override
    public void write(EmptyKey key, HolderLookup.Provider registries, CompoundTag tag) {
    }

    @Override
    public EmptyKey read(RegistryFriendlyByteBuf buf) {
        return (EmptyKey) GenericKey.EMPTY;
    }

    @Override
    public void write(EmptyKey key, RegistryFriendlyByteBuf buf) {
    }

    @Override
    public Codec<EmptyKey> codec() {
        return Codec.unit((EmptyKey) EmptyKey.EMPTY);
    }
}
