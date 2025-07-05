package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import mekanism.common.capabilities.Capabilities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlockEntities;
import ru.zznty.create_factory_logistics.logistics.abstractions.packager.AbstractPackagerBlock;
import ru.zznty.create_factory_logistics.logistics.abstractions.packager.AbstractPackagerBlockEntity;

public class BarrelPackagerBlock extends AbstractPackagerBlock {
    public BarrelPackagerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean prefersBlockEntity(BlockEntity blockEntity) {
        return blockEntity.getCapability(Capabilities.GAS_HANDLER).isPresent() ||
                blockEntity.getCapability(Capabilities.PIGMENT_HANDLER).isPresent() ||
                blockEntity.getCapability(Capabilities.SLURRY_HANDLER).isPresent() ||
                blockEntity.getCapability(Capabilities.INFUSION_HANDLER).isPresent();
    }

    @Override
    public BlockEntityType<? extends AbstractPackagerBlockEntity> getBlockEntityType() {
        return FactoryMekanismBlockEntities.BARREL_PACKAGER.get();
    }

    @Override
    protected Class<? extends AbstractPackagerBlockEntity> getBeClass() {
        return BarrelPackagerBlockEntity.class;
    }
}
