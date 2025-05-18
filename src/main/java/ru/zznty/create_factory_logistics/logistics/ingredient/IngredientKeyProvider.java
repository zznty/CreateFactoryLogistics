package ru.zznty.create_factory_logistics.logistics.ingredient;

import org.jetbrains.annotations.NotNull;

/**
 * Provides support for a specific implementation of {@link IngredientKey}
 */
@SuppressWarnings("rawtypes")
public interface IngredientKeyProvider extends Comparable<IngredientKeyProvider> {
    /**
     * Returns default (empty) instance of the specific key implementation
     *
     * @return Default key instance
     */
    <K extends IngredientKey> K defaultKey();

    <T, K extends IngredientKey<T>> K wrap(T value);

    <K extends IngredientKey> IngredientKeySerializer<K> serializer();

    <K extends IngredientKey> int compare(K a, K b);

    <T> CapabilityFactory<T> capabilityFactory();

    /**
     * Returns UID of the ingredient type registered in JEI {@link mezz.jei.api.runtime.IIngredientManager}
     *
     * @return UID of the ingredient type for JEI
     */
    String ingredientTypeUid();

    @Override
    default int compareTo(@NotNull IngredientKeyProvider o) {
        if (equals(o)) return 0;
        return -1;
    }
}
