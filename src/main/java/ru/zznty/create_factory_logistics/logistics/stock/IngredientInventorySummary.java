package ru.zznty.create_factory_logistics.logistics.stock;

import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;

import java.util.List;

public interface IngredientInventorySummary {
    void add(BoardIngredient ingredient);

    void add(IngredientInventorySummary summary);

    int getCountOf(IngredientKey key);

    List<BoardIngredient> get();

    boolean isEmpty();

    int getCountOf(BigIngredientStack stack);

    boolean erase(IngredientKey key);
}
