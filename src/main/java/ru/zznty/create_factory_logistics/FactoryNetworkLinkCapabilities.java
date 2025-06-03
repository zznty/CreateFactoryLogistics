package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkFluidHandler;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkItemHandler;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkCapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkMode;

public class FactoryNetworkLinkCapabilities {
    public static void register() {
        NetworkLinkCapabilityFactory.FACTORY_MAP.put(
                GenericContentExtender.REGISTRATIONS.get(ItemKey.class), FactoryNetworkLinkCapabilities::createItem);
        NetworkLinkCapabilityFactory.FACTORY_MAP.put(
                GenericContentExtender.REGISTRATIONS.get(FluidKey.class), FactoryNetworkLinkCapabilities::createFluid);
    }

    private static <T> LazyOptional<T> createItem(Capability<T> cap, NetworkLinkMode mode,
                                                  LogisticallyLinkedBehaviour behaviour) {
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(
                () -> new NetworkItemHandler(behaviour.freqId, mode)));
    }

    private static <T> LazyOptional<T> createFluid(Capability<T> cap, NetworkLinkMode mode,
                                                   LogisticallyLinkedBehaviour behaviour) {
        return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(
                () -> new NetworkFluidHandler(behaviour.freqId, mode)));
    }
}
