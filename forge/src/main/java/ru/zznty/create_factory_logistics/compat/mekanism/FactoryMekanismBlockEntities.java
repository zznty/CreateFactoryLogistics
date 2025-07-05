package ru.zznty.create_factory_logistics.compat.mekanism;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackagerBlockEntity;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackagerRenderer;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackagerVisual;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBlockEntity;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelRenderer;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

public class FactoryMekanismBlockEntities {
    public static final BlockEntityEntry<FactoryChemicalPanelBlockEntity> FACTORY_CHEMICAL_PANEL =
            REGISTRATE.blockEntity("factory_chemical_panel", FactoryChemicalPanelBlockEntity::new)
                    .validBlocks(FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE)
                    .renderer(() -> FactoryChemicalPanelRenderer::new)
                    .register();

    public static final BlockEntityEntry<BarrelPackagerBlockEntity> BARREL_PACKAGER = REGISTRATE
            .blockEntity("barrel_packager", BarrelPackagerBlockEntity::new)
            .visual(() -> BarrelPackagerVisual::new, true)
            .validBlocks(FactoryMekanismBlocks.BARREL_PACKAGER)
            .renderer(() -> BarrelPackagerRenderer::new)
            .register();

    public static void register() {
    }
}
