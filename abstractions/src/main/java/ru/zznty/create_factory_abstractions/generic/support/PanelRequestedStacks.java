package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public record PanelRequestedStacks(GenericStack result, List<GenericStack> ingredients,
                                   List<BigItemStack> craftingContext,
                                   String recipeAddress) {
    /*
     * Gets a request equivalent of panel recipe
     */
    public static PanelRequestedStacks of(FactoryPanelBehaviour source) {
        List<GenericStack> ingredients = source.targetedBy
                .values().stream().map(pos ->
                                               GenericStack.of(FactoryPanelBehaviour.at(
                                                               source.getWorld(),
                                                               pos))
                                                       .withAmount(pos.amount)).toList();
        return new PanelRequestedStacks(GenericStack.of(source).withAmount(source.recipeOutput),
                                        ingredients, source.activeCraftingArrangement.isEmpty() ?
                                                     List.of() :
                                                     source.activeCraftingArrangement
                                                             .stream()
                                                             .map(craftingStack -> new BigItemStack(
                                                                     craftingStack.copyWithCount(1)))
                                                             .toList(), source.recipeAddress);
    }

    public boolean hasCraftingContext() {
        return !craftingContext.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PanelRequestedStacks panelRequestedIngredient &&
                panelRequestedIngredient.result.equals(result);
    }

    @Override
    public int hashCode() {
        return result.hashCode();
    }
}
