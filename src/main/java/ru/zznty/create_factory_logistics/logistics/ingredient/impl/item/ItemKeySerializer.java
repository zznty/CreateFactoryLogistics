package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class ItemKeySerializer implements IngredientKeySerializer<ItemIngredientKey> {
    @Override
    public void write(HolderLookup.Provider levelRegistryAccess, ItemIngredientKey key, CompoundTag tag) {
        if (!key.stack().isEmpty())
            key.stack().save(levelRegistryAccess, tag);
    }

    @Override
    public void write(ItemIngredientKey key, RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, key.stack());
    }

    @Override
    public ItemIngredientKey read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        return new ItemIngredientKey(ItemStack.parseOptional(levelRegistryAccess, tag));
    }

    @Override
    public ItemIngredientKey read(RegistryFriendlyByteBuf buf) {
        return new ItemIngredientKey(ItemStack.STREAM_CODEC.decode(buf));
    }
}
