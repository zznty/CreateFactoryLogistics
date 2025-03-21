package ru.zznty.create_factory_logistics.logistics.stock;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;

import java.util.Collection;
import java.util.List;

public interface IFluidInventorySummary {
    void add(BigIngredientStack stack);

    void add(BoardIngredient ingredient, int amount);

    void add(FluidStack stack);

    void add(FluidStack stack, int amount);

    Collection<FluidBoardIngredient> getFluids();

    int getCountOf(Fluid fluid);

    int getCountOf(BigIngredientStack stack);

    List<BigIngredientStack> get();

    boolean isEmpty();

    boolean erase(BoardIngredient ingredient);
}
