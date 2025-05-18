package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;

@ApiStatus.Internal
// I don't want to decompose item stack to get rid of amount info
public record ItemIngredientKey(ItemStack stack) implements IngredientKey<ItemStack> {
    @Override
    public IngredientKeyProvider provider() {
        return IngredientProviders.ITEM.get();
    }

    @Override
    public ItemStack get() {
        return stack;
    }

    @Override
    public IngredientKey<?> genericCopy() {
        return IngredientKey.of(stack.getItem().getDefaultInstance());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemIngredientKey other &&
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
