package ru.zznty.create_factory_logistics;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.zznty.create_factory_logistics.data.FactoryDataGen;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry;

@Mod(CreateFactoryLogistics.MODID)
public class CreateFactoryLogistics {
    public static final String MODID = "create_factory_logistics";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab("create_factory_logistics_tab",
                    t -> t.icon(() -> FactoryItems.REGULAR_JAR.get().getDefaultInstance()))
            .build();

    public CreateFactoryLogistics(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);
        IngredientRegistry.BOARD_INGREDIENTS.register(modEventBus);
        FactoryRecipes.REGISTER.register(modEventBus);

        modEventBus.addListener(FactoryEntities::registerEntityAttributes);
        modEventBus.addListener(FactoryDataGen::gatherData);
        modEventBus.addListener(CreateFactoryLogistics::init);

        FactoryModels.register();
        FactoryItems.register();
        FactoryEntities.register();
        FactoryBlockEntities.register();
        FactoryBlocks.register();
        FactoryMenus.register();
        IngredientProviders.register();

        context.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(FactoryInventoryIdentifiers::register);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
