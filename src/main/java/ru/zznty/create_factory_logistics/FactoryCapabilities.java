package ru.zznty.create_factory_logistics;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackagerAttachedHandler;

public final class FactoryCapabilities {
    public static final Capability<PackagerAttachedHandler> PACKAGER_ATTACHED = CapabilityManager.get(new CapabilityToken<>() {
    });
}
