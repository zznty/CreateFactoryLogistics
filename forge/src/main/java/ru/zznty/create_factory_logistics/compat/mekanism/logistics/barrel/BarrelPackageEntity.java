package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlock;
import com.simibubi.create.foundation.utility.CreateLang;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismEntities;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.abstractions.box.AbstractPackageEntity;
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
        ChemicalStack chemical = FactoryChemicalPanelBehaviour.getChemicalStack(box);
        if (!chemical.isEmpty())
            chemicalLevel.chase(chemical.getAmount(), .5, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void onInsideBlock(BlockState state) {
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        IChemicalHandler cap = box.getCapability(Capabilities.CHEMICAL.item());
        if (cap != null)
            return containedChemicalTooltip(tooltip, isPlayerSneaking, cap);
        return false;
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

    /*public static BarrelPackageEntity spawn(PlayMessages.SpawnEntity spawnEntity, Level world) {
        BarrelPackageEntity barrelPackageEntity =
                new BarrelPackageEntity(world, spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ());
        barrelPackageEntity.setDeltaMovement(spawnEntity.getVelX(), spawnEntity.getVelY(), spawnEntity.getVelZ());
        barrelPackageEntity.clientPosition = barrelPackageEntity.position();
        return barrelPackageEntity;
    }*/

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        @SuppressWarnings("unchecked")
        EntityType.Builder<PackageEntity> boxBuilder = (EntityType.Builder<PackageEntity>) builder;
        return boxBuilder
                /*.setCustomClientFactory(BarrelPackageEntity::spawn)*/
                .sized(1, 1);
    }

    boolean containedChemicalTooltip(List<Component> tooltip, boolean isPlayerSneaking,
                                  IChemicalHandler handler) {
        if (handler == null)
            return false;

        if (handler.getChemicalTanks() == 0)
            return false;

        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);

        boolean isEmpty = true;
        for (int i = 0; i < handler.getChemicalTanks(); i++) {
            ChemicalStack chemicalStack = handler.getChemicalInTank(i);
            if (chemicalStack.isEmpty())
                continue;

            CreateLang.builder()
                    .add(chemicalStack.getTextComponent())
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);

            CreateLang.builder()
                    .add(CreateLang.number(chemicalStack.getAmount())
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(handler.getChemicalTankCapacity(i))
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            isEmpty = false;
        }

        if (handler.getChemicalTanks() > 1) {
            if (isEmpty)
                tooltip.remove(tooltip.size() - 1);
            return true;
        }

        if (!isEmpty)
            return true;

        CreateLang.translate("gui.goggles.fluid_container.capacity")
                .add(CreateLang.number(handler.getChemicalTankCapacity(0))
                        .add(mb)
                        .style(ChatFormatting.GOLD))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        return true;
    }
}
