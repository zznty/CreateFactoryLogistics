package ru.zznty.create_factory_logistics;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.ClientContentRegistration;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.CommonContentRegistration;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericContentExtension;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericClientProvider;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericExtension;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKeySerializer;

public class FactoryGenericExtension extends GenericContentExtension {

    protected FactoryGenericExtension() {
        super(CreateFactoryLogistics.MODID);
    }

    public static void register() {
        // super will register the instance
        new FactoryGenericExtension();
    }

    @Override
    public void registerCommon(CommonContentRegistration registration) {
        registration.register("fluid", FluidKey.class, builder ->
                builder.provider(FluidGenericExtension::new)
                        .serializer(FluidKeySerializer::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void registerClient(ClientContentRegistration registration) {
        registration.<FluidKey>register("fluid", builder ->
                builder.clientProvider(FluidGenericClientProvider::new));
    }
}
