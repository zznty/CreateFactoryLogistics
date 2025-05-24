package ru.zznty.create_factory_abstractions.api.generic.stack;

import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

public record GenericStack(GenericKey key, int amount) {
    public static final GenericStack EMPTY = new GenericStack(GenericKey.EMPTY, 0);

    public boolean isEmpty() {
        return amount == 0 || key == GenericKey.EMPTY;
    }

    public GenericStack withAmount(int amount) {
        if (key == GenericKey.EMPTY) return EMPTY;
        return new GenericStack(key, amount);
    }

    public boolean canStack(GenericStack ingredient) {
        return key.equals(ingredient.key);
    }

    public boolean canStack(GenericKey otherKey) {
        return key.equals(otherKey);
    }

    public static GenericStack wrap(ItemStack stack) {
        return new GenericStack(new ItemKey(stack), stack.getCount());
    }
}
