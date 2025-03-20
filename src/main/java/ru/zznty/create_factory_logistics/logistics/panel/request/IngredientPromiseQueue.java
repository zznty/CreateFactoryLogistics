package ru.zznty.create_factory_logistics.logistics.panel.request;

public interface IngredientPromiseQueue {
    void add(BigIngredientStack stack);

    void forceClear(BoardIngredient ingredient);

    int getTotalPromisedAndRemoveExpired(BoardIngredient ingredient, int expiryTime);

    void ingredientEnteredSystem(BoardIngredient ingredient);
}
