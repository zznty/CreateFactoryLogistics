package ru.zznty.create_factory_logistics.compat.mekanism;

import com.tterrag.registrate.util.entry.ItemEntry;
import ru.zznty.create_factory_logistics.Builders;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelPackageItem;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelStyles;

public class FactoryMekanismItems {

    public static final ItemEntry<BarrelPackageItem> REGULAR_BARREL = Builders.barrel(BarrelStyles.REGULAR).register();

    public static void register() {
    }
}
