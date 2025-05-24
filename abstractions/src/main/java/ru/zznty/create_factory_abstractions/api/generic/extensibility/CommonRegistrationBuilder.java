package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

import java.util.function.Supplier;

public interface CommonRegistrationBuilder<Key extends GenericKey> {
    <Value> CommonRegistrationBuilder<Key> provider(Supplier<GenericKeyProviderExtension<Key, Value>> provider);

    CommonRegistrationBuilder<Key> serializer(Supplier<GenericKeySerializer<Key>> provider);
}
