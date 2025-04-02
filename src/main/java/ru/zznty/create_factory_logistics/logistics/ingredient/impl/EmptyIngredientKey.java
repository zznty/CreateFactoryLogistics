package ru.zznty.create_factory_logistics.logistics.ingredient.impl;

import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;

@ApiStatus.Internal
public record EmptyIngredientKey() implements IngredientKey {
    @Override
    public IngredientKeyProvider provider() {
        return IngredientProviders.EMPTY.get();
    }

    @Override
    public IngredientKey genericCopy() {
        return this;
    }
}
