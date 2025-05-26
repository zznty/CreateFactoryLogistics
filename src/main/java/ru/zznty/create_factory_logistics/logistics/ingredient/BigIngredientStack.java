package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/**
 * Ingredient wrapper over Create's BigItemStack
 * NOTE: As count is always synchronized with ingredient amount, BoardIngredient should be passed in new apis instead of this wrapper
 * TODO: Find a way to batch rewrite all access to BigItemStack.stack and BigItemStack.count to use ingredients
 */
public interface BigIngredientStack {
    BoardIngredient ingredient();

    void setIngredient(BoardIngredient ingredient);

    void setCount(int count);

    int getCount();

    boolean isInfinite();

    BigItemStack asStack();

    Comparator<BigIngredientStack> COMPARATOR = Comparator.comparingInt(BigIngredientStack::getCount)
            .reversed()
            .thenComparing((a, b) -> IngredientKey.COMPARATOR.compare(a.ingredient().key(), b.ingredient().key()));

    static BigIngredientStack of(BoardIngredient ingredient) {
        return of(ingredient, ingredient.amount());
    }

    static BigIngredientStack of(BoardIngredient ingredient, int count) {
        BigItemStack stack = new BigItemStack(ItemStack.EMPTY);

        BigIngredientStack ingredientStack = (BigIngredientStack) stack;
        ingredientStack.setIngredient(ingredient.withAmount(count));

        return ingredientStack;
    }
}
