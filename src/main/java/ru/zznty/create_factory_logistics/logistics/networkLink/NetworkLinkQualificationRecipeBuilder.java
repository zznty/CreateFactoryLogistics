package ru.zznty.create_factory_logistics.logistics.networkLink;

import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

public class NetworkLinkQualificationRecipeBuilder implements RecipeBuilder {
    public void save(RecipeOutput recipeOutput, ResourceLocation key) {
        recipeOutput.accept(CreateFactoryLogistics.resource("network_link_qualification_" + key.toDebugFileName()),
                new NetworkLinkQualificationRecipe(key, CraftingBookCategory.MISC), null);
    }

    @Override
    public RecipeBuilder unlockedBy(String s, Criterion<?> criterion) {
        return null;
    }

    @Override
    public RecipeBuilder group(@org.jetbrains.annotations.Nullable String s) {
        return null;
    }

    @Override
    public Item getResult() {
        return null;
    }
}
