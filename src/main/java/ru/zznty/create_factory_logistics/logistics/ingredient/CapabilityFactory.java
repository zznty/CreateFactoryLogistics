package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkMode;

@FunctionalInterface
public interface CapabilityFactory<T> {
    LazyOptional<T> create(Capability<T> cap, NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour);
}
