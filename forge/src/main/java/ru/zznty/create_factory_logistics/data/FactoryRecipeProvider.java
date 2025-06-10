package ru.zznty.create_factory_logistics.data;

import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.function.Consumer;

public abstract class FactoryRecipeProvider extends CreateRecipeProvider {
    public FactoryRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> p_200404_1_) {
        all.forEach(c -> c.register(p_200404_1_));
        CreateFactoryLogistics.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
    }
}
