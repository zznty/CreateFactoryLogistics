package ru.zznty.create_factory_logistics;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import ru.zznty.create_factory_logistics.data.FactoryDataGen;

@Mod(CreateFactoryLogistics.MODID)
public class CreateFactoryLogistics {
    public static final String MODID = "create_factory_logistics";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab("create_factory_logistics_tab",
                                t -> t.icon(() -> FactoryItems.REGULAR_JAR.get().getDefaultInstance()))
            .build();

    public CreateFactoryLogistics(IEventBus modEventBus, ModContainer modContainer) {
        FactoryGenericExtension.register();

        REGISTRATE.registerEventListeners(modEventBus);
        FactoryRecipes.REGISTER.register(modEventBus);
        FactoryArmInteractionPointTypes.ARM_INTERACTION_POINT_TYPES.register(modEventBus);
        FactoryDataComponents.DATA_COMPONENTS.register(modEventBus);
        FactoryGenericAttributeTypes.REGISTER.register(modEventBus);

        modEventBus.addListener(FactoryEntities::registerEntityAttributes);
        modEventBus.addListener(EventPriority.HIGHEST, FactoryDataGen::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, FactoryDataGen::gatherData);
        modEventBus.addListener(CreateFactoryLogistics::init);
        modEventBus.addListener(FactoryCapabilities::register);

        FactoryModels.register();
        FactoryItems.register();
        FactoryEntities.register();
        FactoryBlockEntities.register();
        FactoryBlocks.register();
        FactoryMenus.register();
        FactoryPackets.register();

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(FactoryInventoryIdentifiers::register);
        event.enqueueWork(FactoryNetworkLinkCapabilities::register);
        event.enqueueWork(FactoryJarUnpackingHandlers::register);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
