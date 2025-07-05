package ru.zznty.create_factory_logistics.logistics.abstractions.box;

import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class AbstractPackageEntity extends PackageEntity {
    public AbstractPackageEntity(EntityType<?> entityTypeIn,
                                 Level worldIn) {
        super(entityTypeIn, worldIn);
    }
}
