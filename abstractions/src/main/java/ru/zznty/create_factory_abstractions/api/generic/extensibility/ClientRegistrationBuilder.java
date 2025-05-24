package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientProvider;

import java.util.function.Supplier;

public interface ClientRegistrationBuilder<Key extends GenericKey> {
    ClientRegistrationBuilder<Key> clientProvider(Supplier<GenericKeyClientProvider<Key>> provider);
}
