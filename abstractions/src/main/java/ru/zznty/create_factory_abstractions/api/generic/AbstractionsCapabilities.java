package ru.zznty.create_factory_abstractions.api.generic;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;

public class AbstractionsCapabilities {
    public static final Capability<PackagerAttachedHandler> PACKAGER_ATTACHED = CapabilityManager.get(
            new CapabilityToken<>() {
            });
}
