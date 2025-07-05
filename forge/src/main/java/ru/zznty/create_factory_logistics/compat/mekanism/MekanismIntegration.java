package ru.zznty.create_factory_logistics.compat.mekanism;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.CapabilityAttacher;

public class MekanismIntegration {
    public static void register(IEventBus bus) {
        FactoryMekanismBlocks.register();
        FactoryMekanismBlockEntities.register();
        FactoryMekanismModels.register();
        FactoryMekanismEntities.register();
        FactoryMekanismItems.register();

        bus.addListener(FactoryMekanismEntities::registerEntityAttributes);

        MinecraftForge.EVENT_BUS.register(CapabilityAttacher.class);
    }
}
