package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

@ApiStatus.Internal
public final class IngredientRegistry {
    public static final String EMPTY_KEY = "empty_ingredient_key";

    public static final DeferredRegister<IngredientKeyProvider> BOARD_INGREDIENTS =
            DeferredRegister.create(CreateFactoryLogistics.resource("board_ingredients"), CreateFactoryLogistics.MODID);
    public static final Registry<IngredientKeyProvider> REGISTRY = BOARD_INGREDIENTS.makeRegistry(b ->
            b.defaultKey(CreateFactoryLogistics.resource(EMPTY_KEY)));
}
