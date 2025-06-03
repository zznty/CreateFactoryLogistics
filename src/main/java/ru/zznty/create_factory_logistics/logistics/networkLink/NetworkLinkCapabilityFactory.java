package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;

import java.util.IdentityHashMap;
import java.util.Map;

@FunctionalInterface
public interface NetworkLinkCapabilityFactory {
    Map<GenericKeyRegistration, NetworkLinkCapabilityFactory> FACTORY_MAP = new IdentityHashMap<>();

    <T> LazyOptional<T> create(Capability<T> cap, NetworkLinkMode mode,
                               LogisticallyLinkedBehaviour behaviour);
}
