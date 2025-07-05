package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientGuiHandler;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientRenderHandler;

public class ChemicalGenericClientProvider implements GenericKeyClientProvider<ChemicalKey> {
    private final ChemicalClientGuiHandler guiHandler = new ChemicalClientGuiHandler();
    private final ChemicalClientRenderHandler renderHandler = new ChemicalClientRenderHandler();

    @Override
    public GenericKeyClientGuiHandler<ChemicalKey> guiHandler() {
        return guiHandler;
    }

    @Override
    public GenericKeyClientRenderHandler<ChemicalKey> renderHandler() {
        return renderHandler;
    }
}
