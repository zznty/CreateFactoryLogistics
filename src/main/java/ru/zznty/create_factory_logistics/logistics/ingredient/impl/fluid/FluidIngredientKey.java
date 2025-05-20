package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;

@ApiStatus.Internal
public record FluidIngredientKey(Holder<Fluid> fluid,
                                 PatchedDataComponentMap components) implements IngredientKey<FluidStack> {
    @Override
    public IngredientKeyProvider provider() {
        return IngredientProviders.FLUID.get();
    }

    @Override
    public FluidStack get() {
        return stack();
    }

    @Override
    public IngredientKey<?> genericCopy() {
        return this;
    }

    public FluidStack stack() {
        return new FluidStack(fluid, 1, components.asPatch());
    }
}
