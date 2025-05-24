package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

import java.util.Comparator;

public interface GenericKeyProviderExtension<Key extends GenericKey, Value> extends Comparator<Key> {
    Key defaultKey();

    Key wrap(Value value);

    Key wrapGeneric(Value value);

    Value unwrap(Key key);
}
