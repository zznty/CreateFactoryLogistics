package ru.zznty.create_factory_logistics.logistics.composite;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.mixin.accessor.PackageEntityAccessor;

import java.util.List;

public class CompositePackageEntity extends PackageEntity {
    public List<ItemStack> children = List.of();

    public CompositePackageEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public CompositePackageEntity(Level worldIn, double x, double y, double z) {
        this(FactoryEntities.COMPOSITE_PACKAGE.get(), worldIn);
        this.setPos(x, y, z);
        this.refreshDimensions();
    }

    @Override
    public void setBox(ItemStack box) {
        super.setBox(box);
        children = CompositePackageItem.getChildren(box);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        super.dropAllDeathLoot(pDamageSource);
        for (ItemStack child : children) {
            ItemEntity jarEntity = new ItemEntity(level(), position().x, position().y, position().z, child);
            level().addFreshEntity(jarEntity);
        }
    }

    public static PackageEntity fromDroppedItem(Level world, Entity originalEntity, ItemStack itemstack) {
        PackageEntity packageEntity = FactoryEntities.COMPOSITE_PACKAGE.get()
                .create(world);

        Vec3 position = originalEntity.position();
        packageEntity.setPos(position);
        packageEntity.setBox(itemstack);
        packageEntity.setDeltaMovement(originalEntity.getDeltaMovement()
                                               .scale(1.5f));
        PackageEntityAccessor accessor = (PackageEntityAccessor) packageEntity;
        accessor.setOriginalEntity(originalEntity);

        if (world != null && !world.isClientSide)
            if (ChuteBlock.isChute(world.getBlockState(BlockPos.containing(position.x, position.y + .5f, position.z))))
                packageEntity.setYRot(((int) packageEntity.getYRot()) / 90 * 90);

        return packageEntity;
    }

    public static CompositePackageEntity spawn(PlayMessages.SpawnEntity spawnEntity, Level world) {
        CompositePackageEntity packageEntity =
                new CompositePackageEntity(world, spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ());
        packageEntity.setDeltaMovement(spawnEntity.getVelX(), spawnEntity.getVelY(), spawnEntity.getVelZ());
        packageEntity.clientPosition = packageEntity.position();
        return packageEntity;
    }

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        @SuppressWarnings("unchecked")
        EntityType.Builder<PackageEntity> boxBuilder = (EntityType.Builder<PackageEntity>) builder;
        return boxBuilder.setCustomClientFactory(CompositePackageEntity::spawn)
                .sized(1, 1);
    }
}
