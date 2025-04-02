package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.function.Supplier;

@ApiStatus.Internal
public final class IngredientRegistry {
    public static final String EMPTY_KEY = "empty_ingredient_key";

    public static final DeferredRegister<IngredientKeyProvider> BOARD_INGREDIENTS =
            DeferredRegister.create(CreateFactoryLogistics.resource("board_ingredients"), CreateFactoryLogistics.MODID);
    public static final Supplier<IForgeRegistry<IngredientKeyProvider>> REGISTRY = BOARD_INGREDIENTS.makeRegistry(() ->
            new RegistryBuilder<IngredientKeyProvider>()
                    .setDefaultKey(CreateFactoryLogistics.resource(EMPTY_KEY))
                    .disableSaving());
}
