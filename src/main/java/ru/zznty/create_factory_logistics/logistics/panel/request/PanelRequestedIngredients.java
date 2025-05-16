package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

import java.util.List;

public record PanelRequestedIngredients(BigIngredientStack result, List<BigIngredientStack> ingredients,
                                        List<BigItemStack> craftingContext,
                                        String recipeAddress) {
    /*
     * Gets a request equivalent of panel recipe
     */
    public static PanelRequestedIngredients of(FactoryPanelBehaviour source) {
        return new PanelRequestedIngredients(BigIngredientStack.of(BoardIngredient.of(source), source.recipeOutput), source.targetedBy.values()
                .stream().map(pos ->
                        BigIngredientStack.of(BoardIngredient.of(FactoryPanelBehaviour.at(source.getWorld(), pos)), pos.amount))
                .toList(), source.activeCraftingArrangement.isEmpty() ?
                List.of() :
                source.activeCraftingArrangement
                        .stream()
                        .map(craftingStack -> new BigItemStack(craftingStack.copyWithCount(1)))
                        .toList(), source.recipeAddress);
    }

    public boolean hasCraftingContext() {
        return !craftingContext.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PanelRequestedIngredients panelRequestedIngredient &&
                panelRequestedIngredient.result.equals(result);
    }

    @Override
    public int hashCode() {
        return result.hashCode();
    }
}
