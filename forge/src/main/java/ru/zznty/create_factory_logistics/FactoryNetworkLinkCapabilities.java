package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;
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
                GenericContentExtender.REGISTRATIONS.get(ItemKey.class), new NetworkLinkCapabilityFactory() {
                    @Override
                    public <T> BlockCapability<T, Direction> capability() {
                        //noinspection unchecked
                        return (BlockCapability<T, Direction>) Capabilities.ItemHandler.BLOCK;
                    }

                    @Override
                    public <T> @Nullable T create(NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour) {
                        //noinspection unchecked
                        return (T) new NetworkItemHandler(behaviour.freqId, mode);
                    }
                });
        NetworkLinkCapabilityFactory.FACTORY_MAP.put(
                GenericContentExtender.REGISTRATIONS.get(FluidKey.class), new NetworkLinkCapabilityFactory() {
                    @Override
                    public <T> BlockCapability<T, Direction> capability() {
                        //noinspection unchecked
                        return (BlockCapability<T, Direction>) Capabilities.FluidHandler.BLOCK;
                    }

                    @Override
                    public <T> @Nullable T create(NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour) {
                        //noinspection unchecked
                        return (T) new NetworkFluidHandler(behaviour.freqId, mode);
                    }
                });
    }
}
