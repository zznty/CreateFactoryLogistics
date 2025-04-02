package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;

@ApiStatus.Internal
public record FluidIngredientKey(Fluid fluid, @Nullable CompoundTag nbt) implements IngredientKey {
    @Override
    public IngredientKeyProvider provider() {
        return IngredientProviders.FLUID.get();
    }

    @Override
    public IngredientKey genericCopy() {
        return this;
    }

    public FluidStack stack() {
        return new FluidStack(fluid, 1, nbt);
    }
}
