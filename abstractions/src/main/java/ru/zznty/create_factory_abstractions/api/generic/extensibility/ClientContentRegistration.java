package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

import java.util.function.Consumer;

public interface ClientContentRegistration {
    <Key extends GenericKey> void register(String id, Consumer<ClientRegistrationBuilder<Key>> builder);
}
