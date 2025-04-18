package ru.zznty.create_factory_logistics;

import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageEntity;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageEntityRender;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageEntity;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageRenderer;

public class FactoryEntities {
    public static final EntityEntry<JarPackageEntity> JAR = register("jar", JarPackageEntity::new, () -> JarPackageRenderer::new,
            MobCategory.MISC, 10, 3, true, false, JarPackageEntity::build)
//            .visual(() -> JarVisual::new, true)
            .register();

    public static final EntityEntry<CompositePackageEntity> COMPOSITE_PACKAGE = register("composite_package", CompositePackageEntity::new,
            () -> CompositePackageEntityRender::new, MobCategory.MISC, 10, 3, true, false, CompositePackageEntity::build)
            .register();

    private static <T extends Entity> CreateEntityBuilder<T, ?> register(String name, EntityType.EntityFactory<T> factory,
                                                                         NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
                                                                         MobCategory group, int range, int updateFrequency, boolean sendVelocity, boolean immuneToFire,
                                                                         NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
        String id = Lang.asId(name);
        return (CreateEntityBuilder<T, ?>) CreateFactoryLogistics.REGISTRATE
                .entity(id, factory, group)
                .properties(b -> b.setTrackingRange(range)
                        .setUpdateInterval(updateFrequency)
                        .setShouldReceiveVelocityUpdates(sendVelocity))
                .properties(propertyBuilder)
                .properties(b -> {
                    if (immuneToFire)
                        b.fireImmune();
                })
                .renderer(renderer);
    }

    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(JAR.get(), JarPackageEntity.createPackageAttributes()
                .build());
        event.put(COMPOSITE_PACKAGE.get(), CompositePackageEntity.createPackageAttributes()
                .build());
    }

    // Load this class

    public static void register() {
    }
}
