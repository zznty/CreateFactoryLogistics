package ru.zznty.create_factory_logistics.logistics.jar;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlock;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.PlayMessages;
import ru.zznty.create_factory_logistics.FactoryEntities;

public class JarPackageEntity extends PackageEntity {
    private Entity originalEntity;

    public LerpedFloat fluidLevel = LerpedFloat.linear();

    public JarPackageEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public JarPackageEntity(Level worldIn, double x, double y, double z) {
        this(FactoryEntities.JAR.get(), worldIn);
        this.setPos(x, y, z);
        this.refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        if (firstTick) {
            verifyInitialEntity();
            originalEntity = null;
        }

        fluidLevel.tickChaser();
    }

    protected void verifyInitialEntity() {
        if (!(originalEntity instanceof ItemEntity itemEntity))
            return;
        CompoundTag nbt = new CompoundTag();
        itemEntity.addAdditionalSaveData(nbt);
        if (nbt.getInt("PickupDelay") != 32767) // See: ItemEntity#makeFakeItem
            return;
        discard();
    }

    @Override
    public void setBox(ItemStack box) {
        super.setBox(box);
        box.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(item -> {
            fluidLevel.chase(item.getFluidInTank(0).getAmount(), .5, LerpedFloat.Chaser.EXP);
        });
    }

    @Override
    protected void onInsideBlock(BlockState state) {
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
    }

    public static JarPackageEntity fromDroppedItem(Level world, Entity originalEntity, ItemStack itemstack) {
        JarPackageEntity jarEntity = (JarPackageEntity) FactoryEntities.JAR.get()
                .create(world);

        Vec3 position = originalEntity.position();
        jarEntity.setPos(position);
        jarEntity.setBox(itemstack);
        jarEntity.setDeltaMovement(originalEntity.getDeltaMovement()
                .scale(1.5f));
        jarEntity.originalEntity = originalEntity;

        if (world != null && !world.isClientSide)
            if (ChuteBlock.isChute(world.getBlockState(BlockPos.containing(position.x, position.y + .5f, position.z))))
                jarEntity.setYRot(((int) jarEntity.getYRot()) / 90 * 90);

        return jarEntity;
    }

    public static JarPackageEntity spawn(PlayMessages.SpawnEntity spawnEntity, Level world) {
        JarPackageEntity jarPackageEntity =
                new JarPackageEntity(world, spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ());
        jarPackageEntity.setDeltaMovement(spawnEntity.getVelX(), spawnEntity.getVelY(), spawnEntity.getVelZ());
        jarPackageEntity.clientPosition = jarPackageEntity.position();
        return jarPackageEntity;
    }

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        @SuppressWarnings("unchecked")
        EntityType.Builder<PackageEntity> boxBuilder = (EntityType.Builder<PackageEntity>) builder;
        return boxBuilder.setCustomClientFactory(JarPackageEntity::spawn)
                .sized(1, 1);
    }
}
