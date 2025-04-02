package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.minecraftforge.registries.RegistryObject;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.EmptyIngredientProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidIngredientProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemIngredientProvider;

import static ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry.BOARD_INGREDIENTS;
import static ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry.EMPTY_KEY;

public final class IngredientProviders {
    public static final RegistryObject<IngredientKeyProvider>
            EMPTY = BOARD_INGREDIENTS.register(EMPTY_KEY, EmptyIngredientProvider::new),
            ITEM = BOARD_INGREDIENTS.register("item", ItemIngredientProvider::new),
            FLUID = BOARD_INGREDIENTS.register("fluid", FluidIngredientProvider::new);

    // Load this class

    public static void register() {
    }
}
