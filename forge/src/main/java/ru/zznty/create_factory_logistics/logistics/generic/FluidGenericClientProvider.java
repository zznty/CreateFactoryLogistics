package ru.zznty.create_factory_logistics.logistics.generic;

import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientGuiHandler;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientRenderHandler;

public class FluidGenericClientProvider implements GenericKeyClientProvider<FluidKey> {
    private final GenericKeyClientRenderHandler<FluidKey> renderHandler = new FluidClientRenderHandler();
    private final GenericKeyClientGuiHandler<FluidKey> guiHandler = new FluidClientGuiHandler();

    @Override
    public GenericKeyClientGuiHandler<FluidKey> guiHandler() {
        return guiHandler;
    }

    @Override
    public GenericKeyClientRenderHandler<FluidKey> renderHandler() {
        return renderHandler;
    }
}
