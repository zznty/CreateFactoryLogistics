package ru.zznty.create_factory_logistics.compat.jei;

public record TransferOperation(int from, int to, mezz.jei.api.ingredients.ITypedIngredient<?> selectedIngredient) {
}
