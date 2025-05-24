package ru.zznty.create_factory_abstractions.generic.impl;

import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.ClientRegistrationBuilder;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientProvider;

import java.util.function.Supplier;

class ClientRegistrationBuilderImpl<Key extends GenericKey> implements ClientRegistrationBuilder<Key> {
    @Nullable
    private Supplier<GenericKeyClientProvider<Key>> provider;

    @Override
    public ClientRegistrationBuilder<Key> clientProvider(Supplier<GenericKeyClientProvider<Key>> provider) {
        if (this.provider != null) {
            throw new IllegalStateException("Provider already set");
        }
        this.provider = provider;
        return this;
    }

    public GenericKeyClientProvider<Key> build() {
        if (provider == null) {
            throw new IllegalStateException("Provider not set");
        }
        return provider.get();
    }
}
