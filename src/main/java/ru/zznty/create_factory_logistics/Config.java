package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.logistics.BigItemStack;
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

    private static final ForgeConfigSpec.IntValue JAR_CAPACITY = BUILDER
            .comment("Capacity of the jar in millibuckets")
            .defineInRange("jarCapacity", 1000, 1, BigItemStack.INF - 1);

    private static final ForgeConfigSpec.BooleanValue JAR_PACKAGER_PREFER_OUTPUTS = BUILDER
            .comment("Whether jar packager (bottler) should pick output tanks over combined. Useful if you want to keep basin inputs intact")
            .define("jarPackagerPrefersOutputs", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean factoryGaugeCascadeRequest;
    public static int jarCapacity;
    public static boolean jarPackagerPrefersOutputs;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.SERVER) return;
        factoryGaugeCascadeRequest = FACTORY_GAUGE_CASCADE_REQUEST.get();
        jarCapacity = JAR_CAPACITY.get();
        jarPackagerPrefersOutputs = JAR_PACKAGER_PREFER_OUTPUTS.get();
    }
}
