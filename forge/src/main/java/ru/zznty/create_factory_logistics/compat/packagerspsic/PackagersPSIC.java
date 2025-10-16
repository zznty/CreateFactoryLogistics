package ru.zznty.create_factory_logistics.compat.packagerspsic;

import net.neoforged.fml.ModList;

public class PackagersPSIC {
    public static boolean isInstalled() {
        return ModList.get().isLoaded("packagerspsic");
    }
}
