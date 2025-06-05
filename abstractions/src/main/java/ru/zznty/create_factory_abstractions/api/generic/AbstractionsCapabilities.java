package ru.zznty.create_factory_abstractions.api.generic;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;

public class AbstractionsCapabilities {
    public static final BlockCapability<PackagerAttachedHandler, Void> PACKAGER_ATTACHED =
            BlockCapability.create(
                    ResourceLocation.fromNamespaceAndPath(CreateFactoryAbstractions.ID, "packager_attached"),
                    PackagerAttachedHandler.class, Void.class);
}
