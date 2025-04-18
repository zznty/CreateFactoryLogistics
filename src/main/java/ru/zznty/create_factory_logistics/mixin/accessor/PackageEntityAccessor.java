package ru.zznty.create_factory_logistics.mixin.accessor;

import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackageEntity.class)
public interface PackageEntityAccessor {
    @Accessor(remap = false)
    Entity getOriginalEntity();

    @Accessor(remap = false)
    void setOriginalEntity(Entity entity);
}
