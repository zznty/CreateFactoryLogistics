package ru.zznty.create_factory_abstractions.api.generic.key;

import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.EmptyKey;

import java.util.Comparator;

public interface GenericKey {
    GenericKey EMPTY = new EmptyKey();

    Comparator<GenericKey> COMPARATOR = (a, b) -> {
        if (a.equals(b)) return 0;
        if (a.equals(EMPTY)) return -1;
        if (b.equals(EMPTY)) return 1;
        GenericKeyRegistration registrationA = GenericContentExtender.registrationOf(a);
        GenericKeyRegistration registrationB = GenericContentExtender.registrationOf(b);
        if (registrationA.equals(registrationB)) return registrationA.provider().compare(a, b);
        return 0;
    };
}
