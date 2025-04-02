package ru.zznty.create_factory_logistics.logistics.panel.request;

import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

import java.util.List;

public interface IngredientGhostMenu {
    BoardIngredient getIngredientInSlot(int slot);

    void setIngredientInSlot(int slot, BoardIngredient ingredient);

    List<BoardIngredient> getIngredients();
}
