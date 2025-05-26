package ru.zznty.create_factory_abstractions.api.generic.key;

public interface GenericKeyClientProvider<Key extends GenericKey> {
    GenericKeyClientGuiHandler<Key> guiHandler();

    GenericKeyClientRenderHandler<Key> renderHandler();
}
