package ru.zznty.create_factory_logistics.logistics.jar;

import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JarStyles {
    public static final float JAR_WINDOW_WIDTH = 3f / 16f;

    // todo more jar styles
    public static final PackageStyles.PackageStyle REGULAR =
            new PackageStyles.PackageStyle("copper_jar", 8, 8, 19f, false);

    public static final List<JarPackageItem> ALL_JARS = new ArrayList<>();

    public static ResourceLocation getItemId(PackageStyles.PackageStyle style) {
        String size = "_" + style.width() + "x" + style.height();
        String id = style.type() + "_package" + (style.rare() ? "" : size);
        return CreateFactoryLogistics.resource(id);
    }

    public static ResourceLocation getRiggingModel(PackageStyles.PackageStyle style) {
        String size = style.width() + "x" + style.height();
        return CreateFactoryLogistics.resource("item/jar/rigging_" + size);
    }

    private static final Random STYLE_PICKER = new Random();

    public static JarPackageItem getRandomJar() {
        return ALL_JARS.get(STYLE_PICKER.nextInt(ALL_JARS.size()));
    }
}
