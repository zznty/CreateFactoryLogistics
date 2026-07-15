package ru.zznty.create_factory_abstractions.logistics.packager;

import net.neoforged.fml.ModList;

public final class PackagersPSIC {
    public static boolean isInstalled() {
        return ModList.get().isLoaded("packagerspsic");
    }

    private PackagersPSIC() {}
}
