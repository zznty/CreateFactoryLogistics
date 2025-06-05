package ru.zznty.create_factory_abstractions;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.registry.TypeRegistries;

@Mod(CreateFactoryAbstractions.ID)
public final class CreateFactoryAbstractions {
    public static final String ID = "create_factory_abstractions";

    public static final boolean EXTENSIBILITY_AVAILABLE = ModList.get().isLoaded("create_factory_logistics");

    public CreateFactoryAbstractions(IEventBus modEventBus, ModContainer modContainer) {
        TypeRegistries.register(modEventBus);
        GenericContentExtender.register(modEventBus);
    }
}
