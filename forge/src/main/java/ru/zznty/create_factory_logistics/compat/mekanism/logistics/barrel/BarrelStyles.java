package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel;

import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BarrelStyles {
    public static final PackageStyles.PackageStyle REGULAR =
            new PackageStyles.PackageStyle("chemical_barrel", 8, 10, 19f, false);

    public static final List<BarrelPackageItem> ALL_BARRELS = new ArrayList<>();

    public static ResourceLocation getRiggingModel(PackageStyles.PackageStyle style) {
        String size = style.width() + "x" + style.height();
        return CreateFactoryLogistics.resource("item/barrel/rigging_" + size);
    }

    private static final Random STYLE_PICKER = new Random();

    public static BarrelPackageItem getRandomBarrel() {
        return ALL_BARRELS.get(STYLE_PICKER.nextInt(ALL_BARRELS.size()));
    }
}
