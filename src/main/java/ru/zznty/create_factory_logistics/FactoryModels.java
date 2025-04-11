package ru.zznty.create_factory_logistics;

import com.simibubi.create.AllPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

public class FactoryModels {
    public static final PartialModel
            FACTORY_FLUID_PANEL = block("factory_fluid_gauge/panel"),
            FACTORY_FLUID_PANEL_WITH_BULB = block("factory_fluid_gauge/panel_with_bulb"),
            FACTORY_FLUID_PANEL_RESTOCKER = block("factory_fluid_gauge/panel_restocker"),
            FACTORY_FLUID_PANEL_RESTOCKER_WITH_BULB = block("factory_fluid_gauge/panel_restocker_with_bulb"),
            FACTORY_FLUID_PANEL_LIGHT = block("factory_fluid_gauge/bulb_light"),
            FACTORY_FLUID_PANEL_RED_LIGHT = block("factory_fluid_gauge/bulb_red"),

    JAR;

    static {
        JAR = PartialModel.of(CreateFactoryLogistics.resource("item/jar/" + JarStyles.REGULAR.type() + "_" + JarStyles.REGULAR.width() + "x" + JarStyles.REGULAR.height()));
        ResourceLocation key = JarStyles.getItemId(JarStyles.REGULAR);
        AllPartialModels.PACKAGES.put(key, JAR);
        AllPartialModels.PACKAGE_RIGGING.put(key, PartialModel.of(JarStyles.getRiggingModel(JarStyles.REGULAR)));
    }

    private static PartialModel block(String path) {
        return PartialModel.of(CreateFactoryLogistics.resource("block/" + path));
    }

    // Load this class

    public static void register() {
    }
}
