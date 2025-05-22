package ru.zznty.create_factory_logistics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CreateFactoryLogistics.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.EnumValue<FontStyle> FONT_STYLE = BUILDER
            .comment("Style of font rendered inside stock keeper ui")
            .defineEnum("fontStyle", FontStyle.CREATE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static FontStyle fontStyle;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.CLIENT || event instanceof ModConfigEvent.Unloading) return;
        fontStyle = FONT_STYLE.get();
    }

    public enum FontStyle {
        SMALL,
        LARGE,
        CREATE
    }
}
