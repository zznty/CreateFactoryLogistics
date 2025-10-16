package ru.zznty.create_factory_logistics.compat.packagerspsic;

import net.minecraftforge.fml.ModList;

public class PackagersPSIC {
    public static boolean isInstalled() {
        return ModList.get().isLoaded("packagerspsic");
    }
}
