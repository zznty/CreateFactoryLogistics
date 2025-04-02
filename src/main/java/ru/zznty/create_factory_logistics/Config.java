package ru.zznty.create_factory_logistics;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CreateFactoryLogistics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue FACTORY_GAUGE_CASCADE_REQUEST = BUILDER
            .comment("Whether to allow factory gauges cascade ingredient requests")
            .define("factoryGaugeCascadeRequest", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean factoryGaugeCascadeRequest;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.SERVER) return;
        factoryGaugeCascadeRequest = FACTORY_GAUGE_CASCADE_REQUEST.get();
    }
}
