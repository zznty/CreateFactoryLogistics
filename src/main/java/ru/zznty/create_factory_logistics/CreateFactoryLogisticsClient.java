package ru.zznty.create_factory_logistics;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import ru.zznty.create_factory_logistics.ponder.PonderPlugin;

import java.util.function.Supplier;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.MODID;

@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateFactoryLogisticsClient {
    @SubscribeEvent
    public static void onClientInit(FMLClientSetupEvent event) {
        BaseConfigScreen.setDefaultActionFor(MODID, base -> base
                .withButtonLabels(null, null, "Gameplay Settings")
                .withSpecs(ClientConfig.SPEC, null, Config.SPEC));

        PonderIndex.addPlugin(new PonderPlugin());
    }

    @SubscribeEvent
    static void onLoadComplete(FMLLoadCompleteEvent event) {
        Supplier<IConfigScreenFactory> configScreen = () -> (mc, previousScreen) -> new BaseConfigScreen(previousScreen, MODID);
        ModList.get().getModContainerById(MODID).get().registerExtensionPoint(IConfigScreenFactory.class, configScreen);
    }
}
