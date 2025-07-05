package ru.zznty.create_factory_logistics.compat.mekanism;

import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;

public class MekanismIntegration {
    public static void register(IEventBus bus) {
        FactoryMekanismBlocks.register();
        FactoryMekanismBlockEntities.register();
        FactoryMekanismModels.register();
        FactoryMekanismEntities.register();
        FactoryMekanismItems.register();

        bus.addListener(FactoryMekanismEntities::registerEntityAttributes);
        bus.addListener(MekanismIntegration::registerCapabilities);
    }

    private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AbstractionsCapabilities.PACKAGER_ATTACHED,
                FactoryMekanismBlockEntities.BARREL_PACKAGER.get(),
                (be, b) -> be.handler);

        event.registerItem(Capabilities.CHEMICAL.item(),
                (stack, b) -> new ComponentBackedChemicalHandler(stack, 1),
                FactoryMekanismItems.REGULAR_BARREL);
    }
}
