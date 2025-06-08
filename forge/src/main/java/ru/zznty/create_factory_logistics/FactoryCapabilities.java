package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkCapabilityFactory;

import java.util.Map;

public final class FactoryCapabilities {
    public static void register(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AbstractionsCapabilities.PACKAGER_ATTACHED, FactoryBlockEntities.JAR_PACKAGER.get(),
                                  (be, b) -> new JarPackagerAttachedHandler(be));

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryBlockEntities.JAR_PACKAGER.get(),
                                  (be, b) -> be.inventory);

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) ->
                                   new FluidHandlerItemStack(FactoryDataComponents.FLUID_CONTENT, stack, Config.jarCapacity),
                           FactoryItems.REGULAR_JAR.get());

        for (Map.Entry<GenericKeyRegistration, NetworkLinkCapabilityFactory> entry : NetworkLinkCapabilityFactory.FACTORY_MAP.entrySet()) {
            event.registerBlockEntity(entry.getValue().capability(), FactoryBlockEntities.NETWORK_LINK.get(),
                                      (be, side) -> {
                                          if (be.provider() == entry.getKey() && (side == null || PackagerLinkBlock.getConnectedDirection(
                                                          be.getBlockState())
                                                  .getOpposite() == side)) {
                                              return entry.getValue().create(be.mode(),
                                                                             be.getBehaviour(
                                                                                     LogisticallyLinkedBehaviour.TYPE));
                                          }
                                          return null;
                                      });
        }
    }
}
