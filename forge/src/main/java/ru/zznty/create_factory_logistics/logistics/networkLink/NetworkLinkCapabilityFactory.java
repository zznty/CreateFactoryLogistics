package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;

import java.util.IdentityHashMap;
import java.util.Map;

public interface NetworkLinkCapabilityFactory {
    Map<GenericKeyRegistration, NetworkLinkCapabilityFactory> FACTORY_MAP = new IdentityHashMap<>();

    <T> BlockCapability<T, Direction> capability();

    <T> @Nullable T create(NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour);
}
