package ru.zznty.create_factory_logistics;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerAttachedHandler;

public final class FactoryCapabilities {
    public static void register(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AbstractionsCapabilities.PACKAGER_ATTACHED, FactoryBlockEntities.JAR_PACKAGER.get(),
                                  (be, b) -> new JarPackagerAttachedHandler(be));

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryBlockEntities.JAR_PACKAGER.get(),
                                  (be, b) -> be.inventory);

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) ->
                                   new FluidHandlerItemStack(FactoryDataComponents.FLUID_CONTENT, stack, Config.jarCapacity),
                           FactoryItems.REGULAR_JAR.get());

        for (GenericKeyRegistration registration : GenericContentExtender.REGISTRATIONS.values()) {
            GenericCapabilityWrapperProvider<Object> provider = registration.provider().capabilityWrapperProvider();
            if (provider == null) continue;
            event.registerBlockEntity(provider.capability(), FactoryBlockEntities.NETWORK_LINK.get(),
                                      (be, side) -> {
                                          if (be.provider() == registration && (side == null || PackagerLinkBlock.getConnectedDirection(
                                                          be.getBlockState())
                                                  .getOpposite() == side)) {
                                              return provider.wrap((summary) -> {
                                                  if (be.mode().includesStored()) {
                                                      summary.add(GenericInventorySummary.of(
                                                              LogisticsManager.getSummaryOfNetwork(be.link.freqId,
                                                                                                   true)));
                                                  }
                                                  if (be.mode().includesPromised()) {
                                                      RequestPromiseQueue queue = Create.LOGISTICS.getQueuedPromises(
                                                              be.link.freqId);
                                                      if (queue != null)
                                                          for (RequestPromise promise : queue.flatten(false)) {
                                                              BigGenericStack stack = BigGenericStack.of(
                                                                      promise.promisedStack);

                                                              if (!stack.get().isEmpty())
                                                                  summary.add(stack.get());
                                                          }
                                                  }
                                              });
                                          }
                                          return null;
                                      });
        }
    }
}
