package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

@ApiStatus.Internal
public class CapabilityAttacher {
    private static final class BarrelPackagerCapabilityProvider implements ICapabilityProvider {
        public static final ResourceLocation IDENTIFIER = CreateFactoryLogistics.resource("barrel_packager_cap");

        private BarrelPackagerCapabilityProvider(BarrelPackagerBlockEntity packagerBE) {
            this.handler = LazyOptional.of(() -> packagerBE.handler);
        }

        private final LazyOptional<PackagerAttachedHandler> handler;

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return AbstractionsCapabilities.PACKAGER_ATTACHED.orEmpty(cap, handler);
        }
    }

    @SubscribeEvent
    public static void attach(final AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof BarrelPackagerBlockEntity be) {
            event.addCapability(BarrelPackagerCapabilityProvider.IDENTIFIER, new BarrelPackagerCapabilityProvider(be));
        }
    }
}
