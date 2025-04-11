package ru.zznty.create_factory_logistics.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

import java.util.List;

public class NetworkLinkQualificationExtension implements ICraftingCategoryExtension {
    private final ResourceLocation key;

    public NetworkLinkQualificationExtension(ResourceLocation key) {
        this.key = key;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        craftingGridHelper.createAndSetOutputs(builder, List.of(NetworkLinkQualificationRecipe.qualifyTo(FactoryBlocks.NETWORK_LINK.asStack(), key)));
        craftingGridHelper.createAndSetInputs(builder, List.of(
                List.of(FactoryBlocks.NETWORK_LINK.asStack()),
                List.of(Ingredient.of(NetworkLinkQualificationRecipe.tag(key)).getItems())
        ), getWidth(), getHeight());
    }

    @Override
    public @Nullable ResourceLocation getRegistryName() {
        return key;
    }
}
