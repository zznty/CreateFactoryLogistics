package ru.zznty.create_factory_logistics.logistics.packager;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.generic.impl.BuiltInPackagerAttachedHandler;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlockEntity;

@ApiStatus.Internal
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CapabilityAttacher {
    private static final TagKey<Block> ITEM_PACKAGER = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(),
                                                                     CreateFactoryLogistics.resource("packager_item"));

    private static final class BuiltInPackagerCapabilityProvider implements ICapabilityProvider {
        public static final ResourceLocation IDENTIFIER = CreateFactoryLogistics.resource("packager_cap");

        private BuiltInPackagerCapabilityProvider(PackagerBlockEntity packagerBE) {
            this.backend = new BuiltInPackagerAttachedHandler(packagerBE);
            this.handler = LazyOptional.of(() -> backend);
        }

        private final BuiltInPackagerAttachedHandler backend;
        private final LazyOptional<PackagerAttachedHandler> handler;

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return AbstractionsCapabilities.PACKAGER_ATTACHED.orEmpty(cap, handler);
        }
    }

    private static final class JarPackagerCapabilityProvider implements ICapabilityProvider {
        public static final ResourceLocation IDENTIFIER = CreateFactoryLogistics.resource("jar_packager_cap");

        private JarPackagerCapabilityProvider(JarPackagerBlockEntity packagerBE) {
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
        if (event.getObject() instanceof PackagerBlockEntity packagerBE) {
            if (packagerBE.getBlockState().is(ITEM_PACKAGER)) {
                final BuiltInPackagerCapabilityProvider provider = new BuiltInPackagerCapabilityProvider(packagerBE);
                event.addCapability(BuiltInPackagerCapabilityProvider.IDENTIFIER, provider);
            } else if (packagerBE instanceof JarPackagerBlockEntity jarPackagerBE && packagerBE.getBlockState().is(
                    FactoryBlocks.JAR_PACKAGER.get())) {
                final JarPackagerCapabilityProvider provider = new JarPackagerCapabilityProvider(jarPackagerBE);
                event.addCapability(JarPackagerCapabilityProvider.IDENTIFIER, provider);
            }
        }
    }

    private CapabilityAttacher() {
    }
}
