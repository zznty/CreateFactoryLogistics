package ru.zznty.create_factory_logistics;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateFactoryLogisticsClient {
    @SubscribeEvent
    public static void onClientInit(FMLClientSetupEvent event) {
        BaseConfigScreen.setDefaultActionFor(MODID, base -> base
                .withButtonLabels(null, null, "Gameplay Settings")
                .withSpecs(ClientConfig.SPEC, null, Config.SPEC));
    }

    @SubscribeEvent
    static void onLoadComplete(FMLLoadCompleteEvent event) {
        ModList.get().getModContainerById(MODID).get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, previousScreen) -> new BaseConfigScreen(previousScreen, MODID)));
    }
}
