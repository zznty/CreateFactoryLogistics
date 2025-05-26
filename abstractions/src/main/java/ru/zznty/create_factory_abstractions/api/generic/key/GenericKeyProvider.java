package ru.zznty.create_factory_abstractions.api.generic.key;

import java.util.Comparator;

public interface GenericKeyProvider<Key extends GenericKey> extends Comparator<Key> {
    Key defaultKey();

    <T> Key wrap(T value);

    <T> Key wrapGeneric(T value);

    <T> T unwrap(Key key);

    String ingredientTypeUid();
}
