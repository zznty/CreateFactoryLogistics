package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

@ApiStatus.Internal
public record ItemKey(ItemStack stack) implements GenericKey {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemKey other &&
                stack.getItem() == other.stack.getItem() &&
                stack.areShareTagsEqual(other.stack);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Item.getId(stack.getItem());
        result = prime * result + stack.getDamageValue();
        result = prime * result + (stack.getTag() == null ? 0 : stack.getTag().hashCode());
        return result;
    }
}
