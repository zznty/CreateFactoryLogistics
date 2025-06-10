package ru.zznty.create_factory_abstractions.api.generic.key;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface GenericKeySerializer<Key extends GenericKey> {
    Key read(HolderLookup.Provider registries, CompoundTag tag);

    void write(Key key, HolderLookup.Provider registries, CompoundTag tag);

    Key read(RegistryFriendlyByteBuf buf);

    void write(Key key, RegistryFriendlyByteBuf buf);

    Codec<Key> codec();
}
