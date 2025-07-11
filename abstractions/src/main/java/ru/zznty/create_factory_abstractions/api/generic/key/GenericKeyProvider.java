package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.resources.ResourceKey;

import java.util.Comparator;
import java.util.Optional;

public interface GenericKeyProvider<Key extends GenericKey> extends Comparator<Key> {
    Key defaultKey();

    <T> Key wrap(T value);

    <T> Key wrapGeneric(T value);

    <T> T unwrap(Key key);

    String ingredientTypeUid(Key key);

    boolean supportsIngredientTypeUid(String uid);

    <T> Optional<ResourceKey<T>> resourceKey(Key key);
}
