package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface GenericKeySerializer<Key extends GenericKey> {
    Key read(CompoundTag tag);

    void write(Key key, CompoundTag tag);

    Key read(FriendlyByteBuf buf);

    void write(Key key, FriendlyByteBuf buf);
}
