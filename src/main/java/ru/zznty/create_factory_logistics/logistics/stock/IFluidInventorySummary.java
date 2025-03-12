package ru.zznty.create_factory_logistics.logistics.stock;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;

import java.util.Collection;
import java.util.List;

public interface IFluidInventorySummary {
    void add(BigIngredientStack stack);

    void add(FluidStack stack);

    Collection<FluidStack> getFluids();

    int getCountOf(Fluid fluid);

    int getCountOf(BigIngredientStack stack);

    List<BigIngredientStack> get();
}
