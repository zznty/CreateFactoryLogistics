package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;
import java.util.UUID;

public record PanelRequestedStacks(GenericStack result, List<StackRequest> ingredients,
                                   List<BigItemStack> craftingContext,
                                   String recipeAddress,
                                   UUID resultNetwork) {
    /*
     * Gets a request equivalent of panel recipe
     */
    public static PanelRequestedStacks of(FactoryPanelBehaviour source) {
        List<StackRequest> ingredients = source.targetedBy
                .values().stream().map(pos -> {
                    FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(
                            source.getWorld(),
                            pos);
                    return new StackRequest(GenericStack.of(behaviour)
                                                    .withAmount(pos.amount),
                                            behaviour.network);
                }).toList();
        return new PanelRequestedStacks(GenericStack.of(source).withAmount(source.recipeOutput),
                                        ingredients, source.activeCraftingArrangement.isEmpty() ?
                                                     List.of() :
                                                     source.activeCraftingArrangement
                                                             .stream()
                                                             .map(craftingStack -> new BigItemStack(
                                                                     craftingStack.copyWithCount(1)))
                                                             .toList(), source.recipeAddress, source.network);
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

