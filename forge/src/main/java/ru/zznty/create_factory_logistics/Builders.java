package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.ItemBuilder;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

import java.util.Locale;

public class Builders {
    public static ItemBuilder<JarPackageItem, CreateRegistrate> jar(PackageStyles.PackageStyle style) {
        String size = "_" + style.width() + "x" + style.height();
        return CreateFactoryLogistics.REGISTRATE.item(JarStyles.getItemId(style).getPath(),
                        p -> new JarPackageItem(p, style))
                .properties(p -> p.stacksTo(1))
                .model((c, p) ->
                        p.withExistingParent(c.getName(), p.modLoc("item/jar/" + style.type() + size)))
                .lang(style.type()
                        .substring(0, 1)
                        .toUpperCase(Locale.ROOT)
                        + style.type()
                        .substring(1));
    }
}
