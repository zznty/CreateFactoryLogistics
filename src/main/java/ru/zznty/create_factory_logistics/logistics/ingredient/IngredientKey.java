package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.EmptyIngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidIngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemIngredientKey;

import java.util.Comparator;

public interface IngredientKey {
    IngredientKey EMPTY = new EmptyIngredientKey();

    IngredientKeyProvider provider();

    /**
     * Gets data-less copy of this key to use as key in map
     * so you won't end up with a lot of complex items being treated as different ones
     */
    IngredientKey genericCopy();

    Comparator<IngredientKey> COMPARATOR = (a, b) -> {
        if (a.equals(b)) return 0;
        if (a.equals(EMPTY)) return -1;
        if (b.equals(EMPTY)) return 1;
        if (a.provider().equals(b.provider())) return a.provider().compare(a, b);
        return 0;
    };

    static IngredientKey of() {
        return EMPTY;
    }

    static IngredientKey of(ItemStack item) {
        // intentionally avoiding checks for count
        if (item.getItem() == Items.AIR) return EMPTY;
        // items are unique
        return new ItemIngredientKey(item.copyWithCount(1));
    }

    static IngredientKey of(FluidStack fluid) {
        // intentionally avoiding checks for amount
        if (fluid.getRawFluid() == Fluids.EMPTY) return EMPTY;
        // fluids depend on nbt (e.g. create potion fluids are different based on nbt)
        return new FluidIngredientKey(fluid.getFluid(), fluid.hasTag() ? fluid.getTag().copy() : null);
    }

}
