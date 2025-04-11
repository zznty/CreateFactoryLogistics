package ru.zznty.create_factory_logistics.logistics.ingredient;

import org.jetbrains.annotations.NotNull;

/**
 * Provides support for a specific implementation of {@link IngredientKey}
 */
public interface IngredientKeyProvider extends Comparable<IngredientKeyProvider> {
    /**
     * Returns default (empty) instance of the specific key implementation
     *
     * @return Default key instance
     */
    <K extends IngredientKey> K defaultKey();

    <K extends IngredientKey> IngredientKeySerializer<K> serializer();

    <K extends IngredientKey> int compare(K a, K b);

    <T> CapabilityFactory<T> capabilityFactory();

    @Override
    default int compareTo(@NotNull IngredientKeyProvider o) {
        if (equals(o)) return 0;
        return -1;
    }
}
