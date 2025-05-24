package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;

@ApiStatus.Internal
public class ItemKeyProvider implements GenericKeyProvider<ItemKey> {
    @Override
    public ItemKey defaultKey() {
        return new ItemKey(ItemStack.EMPTY);
    }

    @Override
    public <T> ItemKey wrap(T value) {
        if (value instanceof ItemStack itemStack)
            return new ItemKey(itemStack.copyWithCount(1));
        throw new IllegalArgumentException("Expected ItemStack, got " + value.getClass());
    }

    @Override
    public <T> ItemKey wrapGeneric(T value) {
        if (value instanceof ItemStack itemStack)
            return new ItemKey(new ItemStack(itemStack.getItemHolder()));
        throw new IllegalArgumentException("Expected ItemStack, got " + value.getClass());
    }

    @Override
    public <T> T unwrap(ItemKey key) {
        //noinspection unchecked
        return (T) key.stack();
    }

    @Override
    public int compare(ItemKey o1, ItemKey o2) {
        return Integer.compare(o1.stack().getItem().hashCode(), o2.stack().getItem().hashCode());
    }
}
