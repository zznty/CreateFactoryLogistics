package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkMode;

public interface CapabilityFactory<T> {
    BlockCapability<T, Direction> capability();

    T create(NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour);
}
