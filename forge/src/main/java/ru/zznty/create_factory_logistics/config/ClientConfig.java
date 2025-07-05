package ru.zznty.create_factory_logistics.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

@EventBusSubscriber(modid = CreateFactoryLogistics.MODID)
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.CLIENT || event instanceof ModConfigEvent.Unloading) return;
    }
}
