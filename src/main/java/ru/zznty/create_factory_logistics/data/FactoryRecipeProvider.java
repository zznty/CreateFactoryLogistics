package ru.zznty.create_factory_logistics.data;

import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.concurrent.CompletableFuture;

public abstract class FactoryRecipeProvider extends CreateRecipeProvider {

    public FactoryRecipeProvider(PackOutput output,
                                 CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput pRecipeOutput) {
        all.forEach(c -> c.register(pRecipeOutput));
        CreateFactoryLogistics.LOGGER.info(
                getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
    }
}
