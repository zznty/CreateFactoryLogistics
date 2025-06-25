package ru.zznty.create_factory_logistics.logistics.jar;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlock;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.PlayMessages;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.logistics.jar.unpack.JarUnpackingHandler;
import ru.zznty.create_factory_logistics.mixin.accessor.PackageEntityAccessor;

import java.util.List;
import java.util.Optional;

public class JarPackageEntity extends PackageEntity implements IHaveGoggleInformation {
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
        fluidLevel.tickChaser();
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
        Pair<FluidStack, ItemStack> pair = GenericItemEmptying.emptyItem(level(), box, true);
        if (pair.getFirst().isEmpty() || !(level() instanceof ServerLevel serverLevel))
            return;
        JarUnpackingHandler handler = JarUnpackingHandler.REGISTRY.get(pair.getFirst().getFluid());
        if (handler == null) handler = JarUnpackingHandler.DEFAULT;

        Player player = null;
        if (pDamageSource.getEntity() instanceof Player entity)
            player = entity;

        handler.unpack(serverLevel, getOnPos(), pair.getFirst(), player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, FluidUtil.getFluidHandler(box).cast());
    }

    public static JarPackageEntity fromDroppedItem(Level world, Entity originalEntity, ItemStack itemstack) {
        JarPackageEntity jarEntity = FactoryEntities.JAR.get()
                .create(world);

        Vec3 position = originalEntity.position();
        jarEntity.setPos(position);
        jarEntity.setBox(itemstack);
        jarEntity.setDeltaMovement(originalEntity.getDeltaMovement()
                                           .scale(1.5f));
        PackageEntityAccessor accessor = (PackageEntityAccessor) jarEntity;
        accessor.setOriginalEntity(originalEntity);

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
