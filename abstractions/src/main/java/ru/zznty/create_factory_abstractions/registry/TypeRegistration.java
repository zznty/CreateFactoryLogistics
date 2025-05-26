package ru.zznty.create_factory_abstractions.registry;

import com.mojang.serialization.Codec;

public interface TypeRegistration<T extends TypeImplementation<T, ?>> {
    Class<? extends T> type();

    Codec<? extends T> codec();
}
