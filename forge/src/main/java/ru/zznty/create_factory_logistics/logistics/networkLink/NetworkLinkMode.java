package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.lang.Lang;
import ru.zznty.create_factory_logistics.FactoryIcons;

public enum NetworkLinkMode implements INamedIconOptions {
    STORED(FactoryIcons.I_NETWORK_LINK_STORED),
    PROMISED(FactoryIcons.I_NETWORK_LINK_PROMISED),
    ALL(FactoryIcons.I_NETWORK_LINK_ALL);

    private final String translationKey;
    private final AllIcons icon;

    NetworkLinkMode(AllIcons icon) {
        this.icon = icon;
        translationKey = "gui.network_link.mode." + Lang.asId(name());
    }

    @Override
    public AllIcons getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    public boolean includesStored() {
        return this == STORED || this == ALL;
    }

    public boolean includesPromised() {
        return this == PROMISED || this == ALL;
    }
}
