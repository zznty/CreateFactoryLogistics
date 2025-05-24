package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

@ApiStatus.Internal
public class ItemKeySerializer implements GenericKeySerializer<ItemKey> {
    @Override
    public ItemKey read(CompoundTag tag) {
        return new ItemKey(ItemStack.of(tag));
    }

    @Override
    public void write(ItemKey key, CompoundTag tag) {
        key.stack().save(tag);
    }

    @Override
    public ItemKey read(FriendlyByteBuf buf) {
        return new ItemKey(buf.readItem());
    }

    @Override
    public void write(ItemKey key, FriendlyByteBuf buf) {
        buf.writeItem(key.stack());
    }
}
