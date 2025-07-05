package ru.zznty.create_factory_logistics.compat.mekanism;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelPackageEntity;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelPackageRenderer;

public class FactoryMekanismEntities {
    public static final EntityEntry<BarrelPackageEntity> BARREL = FactoryEntities.register("barrel",
                                                                                           BarrelPackageEntity::new,
                                                                                           () -> BarrelPackageRenderer::new,
                                                                                           MobCategory.MISC, 10, 3,
                                                                                           true,
                                                                                           false,
                                                                                           BarrelPackageEntity::build)
//            .visual(() -> JarVisual::new, true)
            .register();

    public static void register() {
    }

    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(BARREL.get(), BarrelPackageEntity.createPackageAttributes()
                .build());
    }
}
