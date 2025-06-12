package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

@ApiStatus.Internal
public class ItemKeySerializer implements GenericKeySerializer<ItemKey> {
    @Override
    public ItemKey read(HolderLookup.Provider registries, CompoundTag tag) {
        return new ItemKey(ItemStack.parseOptional(registries, tag));
    }

    @Override
    public void write(ItemKey key, HolderLookup.Provider registries, CompoundTag tag) {
        if (key.stack().isEmpty()) return;
        tag.merge((CompoundTag) key.stack().save(registries));
    }

    @Override
    public ItemKey read(RegistryFriendlyByteBuf buf) {
        return new ItemKey(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    @Override
    public void write(ItemKey key, RegistryFriendlyByteBuf buf) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, key.stack());
    }
}
