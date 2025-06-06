package ru.zznty.create_factory_logistics;

import com.simibubi.create.foundation.gui.AllIcons;
import ru.zznty.create_factory_logistics.render.IconAtlasIndexHolder;

public class FactoryIcons {
    public static final AllIcons
            I_NETWORK_LINK_STORED = next(),
            I_NETWORK_LINK_PROMISED = next(),
            I_NETWORK_LINK_ALL = next();

    private static int x, y;

    private static AllIcons next() {
        AllIcons icon = new AllIcons(x++, y);
        IconAtlasIndexHolder holder = (IconAtlasIndexHolder) icon;
        holder.setIconAtlasIndex(1);
        return icon;
    }
}
