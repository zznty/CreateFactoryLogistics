package ru.zznty.create_factory_abstractions.generic.impl;

import net.createmod.catnip.data.Pair;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.CommonRegistrationBuilder;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericKeyProviderExtension;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

import java.util.function.Supplier;

class CommonRegistrationBuilderImpl<Key extends GenericKey> implements CommonRegistrationBuilder<Key> {
    @Nullable
    private Supplier<GenericKeyProvider<Key>> provider;
    @Nullable
    private Supplier<GenericKeySerializer<Key>> serializer;

    @Override
    public <Value> CommonRegistrationBuilder<Key> provider(Supplier<GenericKeyProviderExtension<Key, Value>> provider) {
        if (this.provider != null) {
            throw new IllegalStateException("Provider already set");
        }
        this.provider = () -> new GenericKeyProvider<>() {
            private final GenericKeyProviderExtension<Key, Value> extension = provider.get();

            @Override
            public Key defaultKey() {
                return extension.defaultKey();
            }

            @Override
            public <T> Key wrap(T value) {
                //noinspection unchecked
                return extension.wrap((Value) value);
            }

            @Override
            public <T> Key wrapGeneric(T value) {
                //noinspection unchecked
                return extension.wrapGeneric((Value) value);
            }

            @Override
            public <T> T unwrap(Key key) {
                //noinspection unchecked
                return (T) extension.unwrap(key);
            }

            @Override
            public int compare(Key o1, Key o2) {
                return extension.compare(o1, o2);
            }
        };
        return this;
    }

    @Override
    public CommonRegistrationBuilder<Key> serializer(Supplier<GenericKeySerializer<Key>> provider) {
        if (this.serializer != null) {
            throw new IllegalStateException("Serializer already set");
        }
        this.serializer = provider;
        return this;
    }

    public Pair<GenericKeyProvider<Key>, GenericKeySerializer<Key>> build() {
        if (provider == null) {
            throw new IllegalStateException("Provider not set");
        }
        if (serializer == null) {
            throw new IllegalStateException("Serializer not set");
        }

        return Pair.of(provider.get(), serializer.get());
    }
}
