package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.logistics.BigItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CreateFactoryLogistics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue FACTORY_GAUGE_CASCADE_REQUEST = BUILDER
            .comment("Whether to allow factory gauges cascade ingredient requests")
            .define("factoryGaugeCascadeRequest", true);

    private static final ModConfigSpec.IntValue JAR_CAPACITY = BUILDER
            .comment("Capacity of the jar in millibuckets")
            .defineInRange("jarCapacity", 1000, 1, BigItemStack.INF - 1);

    private static final ModConfigSpec.BooleanValue JAR_PACKAGER_PREFER_OUTPUTS = BUILDER
            .comment("Whether jar packager (bottler) should pick output tanks over combined. Useful if you want to keep basin inputs intact")
            .define("jarPackagerPrefersOutputs", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean factoryGaugeCascadeRequest;
    public static int jarCapacity;
    public static boolean jarPackagerPrefersOutputs;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.SERVER || event instanceof ModConfigEvent.Unloading) return;
        factoryGaugeCascadeRequest = FACTORY_GAUGE_CASCADE_REQUEST.get();
        jarCapacity = JAR_CAPACITY.get();
        jarPackagerPrefersOutputs = JAR_PACKAGER_PREFER_OUTPUTS.get();
    }
}
