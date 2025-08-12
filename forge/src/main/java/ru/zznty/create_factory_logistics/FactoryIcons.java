package ru.zznty.create_factory_logistics;

import com.simibubi.create.foundation.gui.AllIcons;

import net.neoforged.fml.loading.FMLEnvironment;
import ru.zznty.create_factory_logistics.render.IconAtlasIndexHolder;

public class FactoryIcons {
    public static final AllIcons
            I_NETWORK_LINK_STORED,
            I_NETWORK_LINK_PROMISED,
            I_NETWORK_LINK_ALL;

    private static int x, y;

    static {
        if (FMLEnvironment.dist.isClient()) {
            I_NETWORK_LINK_STORED = next();
            I_NETWORK_LINK_PROMISED = next();
            I_NETWORK_LINK_ALL = next();
        } else {
            I_NETWORK_LINK_STORED = null;
            I_NETWORK_LINK_PROMISED = null;
            I_NETWORK_LINK_ALL = null;
        }
    }

    private static AllIcons next() {
        AllIcons icon = new AllIcons(x++, y);
        ((IconAtlasIndexHolder) icon).setIconAtlasIndex(1);
        return icon;
    }
}
