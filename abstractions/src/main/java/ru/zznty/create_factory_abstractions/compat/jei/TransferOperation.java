package ru.zznty.create_factory_abstractions.compat.jei;

public record TransferOperation(int from, int to, mezz.jei.api.ingredients.ITypedIngredient<?> selectedIngredient) {
}
