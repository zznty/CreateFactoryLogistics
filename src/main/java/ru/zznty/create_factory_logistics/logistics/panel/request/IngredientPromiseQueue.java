package ru.zznty.create_factory_logistics.logistics.panel.request;

import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

public interface IngredientPromiseQueue {
    void add(BigIngredientStack stack);

    void forceClear(BoardIngredient ingredient);

    int getTotalPromisedAndRemoveExpired(BoardIngredient ingredient, int expiryTime);

    void ingredientEnteredSystem(BoardIngredient ingredient);
}
