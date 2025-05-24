package ru.zznty.create_factory_abstractions.api.generic.key;

import ru.zznty.create_factory_abstractions.generic.key.EmptyKey;

public interface GenericKey {
    GenericKey EMPTY = new EmptyKey();
}
