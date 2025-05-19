package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface CraftableIngredientStack extends BigIngredientStack {
    List<BoardIngredient> ingredients();

    List<BoardIngredient> results();

    int outputCount(Level level);

    CraftableBigItemStack asStack();
}
