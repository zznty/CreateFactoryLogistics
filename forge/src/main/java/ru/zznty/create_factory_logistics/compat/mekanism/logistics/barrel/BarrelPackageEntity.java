package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlock;
import mekanism.api.chemical.ChemicalStack;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.PlayMessages;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismEntities;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.abstractions.box.AbstractPackageEntity;
import ru.zznty.create_factory_logistics.logistics.jar.unpack.JarUnpackingHandler;
import ru.zznty.create_factory_logistics.mixin.accessor.PackageEntityAccessor;

import java.util.List;

public class BarrelPackageEntity extends AbstractPackageEntity implements IHaveGoggleInformation {
    public LerpedFloat chemicalLevel = LerpedFloat.linear();

    public BarrelPackageEntity(EntityType<?> entityTypeIn,
                               Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public BarrelPackageEntity(Level worldIn, double x, double y, double z) {
        super(FactoryMekanismEntities.BARREL.get(), worldIn);
        this.setPos(x, y, z);
        this.refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        chemicalLevel.tickChaser();
    }

    @Override
    public void setBox(ItemStack box) {
        super.setBox(box);
        ChemicalStack<?> chemical = FactoryChemicalPanelBehaviour.getChemicalStack(box);
        if (!chemical.isEmpty())
            chemicalLevel.chase(chemical.getAmount(), .5, LerpedFloat.Chaser.EXP);
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

    public static BarrelPackageEntity fromDroppedItem(Level world, Entity originalEntity, ItemStack itemstack) {
        BarrelPackageEntity barrelEntity = FactoryMekanismEntities.BARREL.get()
                .create(world);

        Vec3 position = originalEntity.position();
        barrelEntity.setPos(position);
        barrelEntity.setBox(itemstack);
        barrelEntity.setDeltaMovement(originalEntity.getDeltaMovement()
                                              .scale(1.5f));
        PackageEntityAccessor accessor = (PackageEntityAccessor) barrelEntity;
        accessor.setOriginalEntity(originalEntity);

        if (world != null && !world.isClientSide)
            if (ChuteBlock.isChute(world.getBlockState(BlockPos.containing(position.x, position.y + .5f, position.z))))
                barrelEntity.setYRot(((int) barrelEntity.getYRot()) / 90 * 90);

        return barrelEntity;
    }

    public static BarrelPackageEntity spawn(PlayMessages.SpawnEntity spawnEntity, Level world) {
        BarrelPackageEntity barrelPackageEntity =
                new BarrelPackageEntity(world, spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ());
        barrelPackageEntity.setDeltaMovement(spawnEntity.getVelX(), spawnEntity.getVelY(), spawnEntity.getVelZ());
        barrelPackageEntity.clientPosition = barrelPackageEntity.position();
        return barrelPackageEntity;
    }

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        @SuppressWarnings("unchecked")
        EntityType.Builder<PackageEntity> boxBuilder = (EntityType.Builder<PackageEntity>) builder;
        return boxBuilder.setCustomClientFactory(BarrelPackageEntity::spawn)
                .sized(1, 1);
    }
}
