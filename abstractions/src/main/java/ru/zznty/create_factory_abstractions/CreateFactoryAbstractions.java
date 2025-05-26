package ru.zznty.create_factory_abstractions;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.registry.TypeRegistries;

@Mod(CreateFactoryAbstractions.ID)
public final class CreateFactoryAbstractions {
    public static final String ID = "create_factory_abstractions";

    public static final boolean EXTENSIBILITY_AVAILABLE = ModList.get().isLoaded("create_factory_logistics");

    public CreateFactoryAbstractions(FMLJavaModLoadingContext context) {
        TypeRegistries.register(context.getModEventBus());
        GenericContentExtender.register(context.getModEventBus());
    }
}
