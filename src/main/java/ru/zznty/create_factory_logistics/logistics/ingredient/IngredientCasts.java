package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidIngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemIngredientKey;

public final class IngredientCasts {
    public static @NotNull ItemStack asItemStack(BoardIngredient ingredient) {
        if (ingredient.key() instanceof ItemIngredientKey itemKey) {
            return itemKey.stack().copyWithCount(ingredient.amount());
        }

        return ItemStack.EMPTY;
    }

    public static @NotNull FluidStack asFluidStack(BoardIngredient ingredient) {
        if (ingredient.key() instanceof FluidIngredientKey fluidKey) {
            return FluidHelper.copyStackWithAmount(fluidKey.stack(), ingredient.amount());
        }

        return FluidStack.EMPTY;
    }
}
