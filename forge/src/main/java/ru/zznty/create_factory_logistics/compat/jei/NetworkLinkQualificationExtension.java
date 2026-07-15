package ru.zznty.create_factory_logistics.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

import java.util.List;

public class NetworkLinkQualificationExtension implements ICraftingCategoryExtension<NetworkLinkQualificationRecipe> {
    private static final ResourceLocation EMPTY_KEY =
            ResourceLocation.fromNamespaceAndPath(CreateFactoryAbstractions.ID, "empty");

    @Override
    public void setRecipe(RecipeHolder<NetworkLinkQualificationRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        craftingGridHelper.createAndSetOutputs(builder, List.of(NetworkLinkQualificationRecipe.qualifyTo(FactoryBlocks.NETWORK_LINK.asStack(), recipeHolder.value().key())));

        if (recipeHolder.value().key().equals(EMPTY_KEY)) {
            craftingGridHelper.createAndSetInputs(builder, List.of(
                    List.of(FactoryBlocks.NETWORK_LINK.asStack())
            ), getWidth(recipeHolder), getHeight(recipeHolder));
        } else {
            craftingGridHelper.createAndSetInputs(builder, List.of(
                    List.of(FactoryBlocks.NETWORK_LINK.asStack()),
                    List.of(Ingredient.of(NetworkLinkQualificationRecipe.tag(recipeHolder.value().key())).getItems())
            ), getWidth(recipeHolder), getHeight(recipeHolder));
        }
    }
}
