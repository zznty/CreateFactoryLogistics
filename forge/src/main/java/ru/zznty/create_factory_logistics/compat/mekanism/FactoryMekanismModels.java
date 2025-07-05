package ru.zznty.create_factory_logistics.compat.mekanism;

import com.simibubi.create.AllPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelStyles;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

public class FactoryMekanismModels {
    public static final PartialModel
            FACTORY_CHEMICAL_PANEL = block("factory_chemical_gauge/panel"),
            FACTORY_CHEMICAL_PANEL_WITH_BULB = block("factory_chemical_gauge/panel_with_bulb"),
            FACTORY_CHEMICAL_PANEL_RESTOCKER = block("factory_chemical_gauge/panel_restocker"),
            FACTORY_CHEMICAL_PANEL_RESTOCKER_WITH_BULB = block("factory_chemical_gauge/panel_restocker_with_bulb"),
            FACTORY_CHEMICAL_PANEL_LIGHT = block("factory_chemical_gauge/bulb_light"),
            FACTORY_CHEMICAL_PANEL_RED_LIGHT = block("factory_chemical_gauge/bulb_red"),
            BARREL;

    static {
        BARREL = PartialModel.of(CreateFactoryLogistics.resource(
                "item/barrel/" + BarrelStyles.REGULAR.type() + "_" + BarrelStyles.REGULAR.width() + "x" + BarrelStyles.REGULAR.height()));
        ResourceLocation key = JarStyles.getItemId(BarrelStyles.REGULAR);
        AllPartialModels.PACKAGES.put(key, BARREL);
        AllPartialModels.PACKAGE_RIGGING.put(key, PartialModel.of(BarrelStyles.getRiggingModel(BarrelStyles.REGULAR)));
    }

    private static PartialModel block(String path) {
        return PartialModel.of(CreateFactoryLogistics.resource("block/" + path));
    }

    // Load this class

    public static void register() {
    }
}
