package ru.zznty.create_factory_logistics.logistics.ingredient;

/**
 * Provides support for a specific implementation of {@link IngredientKey}
 */
public interface IngredientKeyProvider {
    /**
     * Returns default (empty) instance of the specific key implementation
     *
     * @return Default key instance
     */
    <K extends IngredientKey> K defaultKey();

    <K extends IngredientKey> IngredientKeySerializer<K> serializer();

    <K extends IngredientKey> int compare(K a, K b);
}
