package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.world.item.ItemStack;

public interface BigIngredientStack {
    BoardIngredient getIngredient();

    void setIngredient(BoardIngredient ingredient);

    void setCount(int count);

    int getCount();

    boolean isInfinite();

    BigItemStack asStack();

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
