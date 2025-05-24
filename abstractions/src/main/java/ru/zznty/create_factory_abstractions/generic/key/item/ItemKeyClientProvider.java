package ru.zznty.create_factory_abstractions.generic.key.item;

import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientGuiHandler;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientProvider;

@ApiStatus.Internal
public class ItemKeyClientProvider implements GenericKeyClientProvider<ItemKey> {
    private final GenericKeyClientGuiHandler<ItemKey> handler = new ItemKeyGuiHandler();

    @Override
    public GenericKeyClientGuiHandler<ItemKey> guiHandler() {
        return handler;
    }
}
