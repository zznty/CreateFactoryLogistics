package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

public abstract class GenericContentExtension {
    protected GenericContentExtension(String modId) {
        GenericContentExtender.enqueueExtension(modId, this);
    }

    public abstract void registerCommon(CommonContentRegistration registration);

    @OnlyIn(Dist.CLIENT)
    public abstract void registerClient(ClientContentRegistration registration);
}
