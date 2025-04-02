package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class ItemKeySerializer implements IngredientKeySerializer<ItemIngredientKey> {
    @Override
    public void write(ItemIngredientKey key, CompoundTag tag) {
        key.stack().save(tag);
    }

    @Override
    public void write(ItemIngredientKey key, FriendlyByteBuf buf) {
        buf.writeItem(key.stack());
    }

    @Override
    public ItemIngredientKey read(CompoundTag tag) {
        return new ItemIngredientKey(ItemStack.of(tag));
    }

    @Override
    public ItemIngredientKey read(FriendlyByteBuf buf) {
        return new ItemIngredientKey(buf.readItem());
    }
}
