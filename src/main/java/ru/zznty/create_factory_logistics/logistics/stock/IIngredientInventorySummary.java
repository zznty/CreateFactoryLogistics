package ru.zznty.create_factory_logistics.logistics.stock;

import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;

import java.util.List;

public interface IIngredientInventorySummary {
    void add(BigIngredientStack stack);

    void add(BoardIngredient ingredient, int amount);

    void add(FluidStack stack);

    void add(FluidStack stack, int amount);

    void add(IIngredientInventorySummary summary);

    int getCountOf(BoardIngredient ingredient);

    int getCountOf(BigIngredientStack stack);

    List<BigIngredientStack> getStacks();

    boolean isEmpty();

    boolean erase(BoardIngredient ingredient);
}
