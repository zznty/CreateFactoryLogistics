package ru.zznty.create_factory_abstractions.registry;

public interface TypeImplementation<T extends TypeImplementation<T, R>, R extends TypeRegistration<T>> {
    R type();
}
