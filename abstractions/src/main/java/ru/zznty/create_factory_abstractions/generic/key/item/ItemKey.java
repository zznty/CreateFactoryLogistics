package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

@ApiStatus.Internal
public record ItemKey(ItemStack stack) implements GenericKey {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemKey(ItemStack otherStack) &&
                ItemStack.isSameItemSameComponents(stack, otherStack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}
