package ru.zznty.create_factory_logistics;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.config.WorldConfig;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerAttachedHandler;

public final class FactoryCapabilities {
    public static void register(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AbstractionsCapabilities.PACKAGER_ATTACHED, FactoryBlockEntities.JAR_PACKAGER.get(),
                                  (be, b) -> new JarPackagerAttachedHandler(be));

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryBlockEntities.JAR_PACKAGER.get(),
                                  (be, b) -> be.inventory);

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) ->
                                   new FluidHandlerItemStack(FactoryDataComponents.FLUID_CONTENT, stack, WorldConfig.jarCapacity),
                           FactoryItems.REGULAR_JAR.get());

        for (GenericKeyRegistration registration : GenericContentExtender.REGISTRATIONS.values()) {
            GenericCapabilityWrapperProvider<Object, Object> provider = registration.provider().capabilityWrapperProvider();
            if (provider == null) continue;
            event.registerBlockEntity(provider.capability(), FactoryBlockEntities.NETWORK_LINK.get(),
                                      (be, side) -> {
                                          if (be.provider() == registration && (side == null || PackagerLinkBlock.getConnectedDirection(
                                                          be.getBlockState())
                                                  .getOpposite() == side)) {
                                              return provider.wrap((summary, registries) -> {
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
                                              }, be.getLevel().registryAccess());
                                          }
                                          return null;
                                      });
        }

        event.registerItem(AbstractionsCapabilities.GENERIC_INVENTORY_ITEM, (stack, b) -> {
            if (stack.getItem() instanceof CompositePackageItem)
                return new CompositePackageInventory(stack);
            if (stack.getItem() instanceof JarPackageItem)
                return new JarPackageInventory(stack);
            return null;
        }, FactoryItems.COMPOSITE_PACKAGE.get(), FactoryItems.REGULAR_JAR.get());
    }

    private record CompositePackageInventory(ItemStack stack) implements GenericInventory {
        @Override
        public @Nullable GenericInventorySummaryProvider get(GenericKeyRegistration registration) {
            if (registration == GenericContentExtender.REGISTRATIONS.get(ItemKey.class))
                return new ItemInventorySummaryProvider(CompositePackageItem.getContents(stack));
            return null;
        }
    }

    private record JarPackageInventory(ItemStack stack) implements GenericInventory {
        @Override
        public @Nullable GenericInventorySummaryProvider get(GenericKeyRegistration registration) {
            if (registration == GenericContentExtender.REGISTRATIONS.get(FluidKey.class)) {
                return (summary, registries) -> FluidUtil.getFluidContained(stack).ifPresent(
                        fluid -> summary.add(FluidGenericStack.wrap(fluid)));
            }
            return null;
        }
    }
}
