package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;

import java.util.List;

public interface CraftableIngredientStack extends BigIngredientStack {
    List<BoardIngredient> ingredients();

    CraftableBigItemStack asStack();
}
