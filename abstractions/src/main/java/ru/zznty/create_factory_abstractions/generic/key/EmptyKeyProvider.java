package ru.zznty.create_factory_abstractions.generic.key;

import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;

@ApiStatus.Internal
public class EmptyKeyProvider implements GenericKeyProvider<EmptyKey> {
    @Override
    public EmptyKey defaultKey() {
        return (EmptyKey) EmptyKey.EMPTY;
    }

    @Override
    public <T> EmptyKey wrap(T value) {
        if (value != GenericKey.EMPTY) {
            throw new IllegalArgumentException("Empty key provider only supports EmptyKey");
        }
        return defaultKey();
    }

    @Override
    public <T> EmptyKey wrapGeneric(T value) {
        return wrap(value);
    }

    @Override
    public <T> T unwrap(EmptyKey key) {
        //noinspection unchecked
        return (T) GenericKey.EMPTY;
    }

    @Override
    public int compare(EmptyKey o1, EmptyKey o2) {
        return 0;
    }
}
