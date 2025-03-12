package ru.zznty.create_factory_logistics;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlockEntity;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerRenderer;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerVisual;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlockEntity;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelRenderer;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

public class FactoryBlockEntities {
    public static final BlockEntityEntry<JarPackagerBlockEntity> JAR_PACKAGER = REGISTRATE
            .blockEntity("jar_packager", JarPackagerBlockEntity::new)
            .visual(() -> JarPackagerVisual::new, true)
            .validBlocks(FactoryBlocks.JAR_PACKAGER)
            .renderer(() -> JarPackagerRenderer::new)
            .register();

    public static final BlockEntityEntry<FactoryFluidPanelBlockEntity> FACTORY_FLUID_PANEL =
            REGISTRATE.blockEntity("factory_fluid_panel", FactoryFluidPanelBlockEntity::new)
                    .validBlocks(FactoryBlocks.FACTORY_FLUID_GAUGE)
                    .renderer(() -> FactoryFluidPanelRenderer::new)
                    .register();

    // Load this class

    public static void register() {
    }
}
