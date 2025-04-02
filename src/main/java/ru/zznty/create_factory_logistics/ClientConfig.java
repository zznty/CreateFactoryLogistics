package ru.zznty.create_factory_logistics;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CreateFactoryLogistics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.EnumValue<FontStyle> FONT_STYLE = BUILDER
            .comment("Style of font rendered inside stock keeper ui")
            .defineEnum("fontStyle", FontStyle.LARGE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static FontStyle fontStyle;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() != ModConfig.Type.CLIENT) return;
        fontStyle = FONT_STYLE.get();
    }

    public enum FontStyle {
        SMALL,
        LARGE,
        CREATE
    }
}
