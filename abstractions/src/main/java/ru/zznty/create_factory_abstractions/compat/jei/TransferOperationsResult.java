package ru.zznty.create_factory_abstractions.compat.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import java.util.ArrayList;
import java.util.List;

public record TransferOperationsResult(List<TransferOperation> results, List<IRecipeSlotView> missingItems) {
    public static TransferOperationsResult create() {
        return new TransferOperationsResult(new ArrayList<>(), new ArrayList<>());
    }
}
