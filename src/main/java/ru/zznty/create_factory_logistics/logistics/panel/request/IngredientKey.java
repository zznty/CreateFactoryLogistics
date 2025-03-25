package ru.zznty.create_factory_logistics.logistics.panel.request;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public interface IngredientKey {
    IngredientKey EMPTY = new EmptyIngredientKey();

    static IngredientKey of() {
        return EMPTY;
    }

    static IngredientKey of(Item item) {
        return new ItemIngredientKey(item);
    }

    static IngredientKey of(Fluid fluid) {
        return new FluidIngredientKey(fluid);
    }

    record EmptyIngredientKey() implements IngredientKey {
    }

    record ItemIngredientKey(Item item) implements IngredientKey {
    }

    record FluidIngredientKey(Fluid fluid) implements IngredientKey {
    }
}
