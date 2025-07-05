package ru.zznty.create_factory_logistics.logistics.jarPackager;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_logistics.logistics.abstractions.packager.AbstractPackagerBlock;
import ru.zznty.create_factory_logistics.logistics.abstractions.packager.AbstractPackagerBlockEntity;

public class JarPackagerBlock extends AbstractPackagerBlock {
    public JarPackagerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean prefersBlockEntity(BlockEntity blockEntity) {
        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent();
    }

    @Override
    public BlockEntityType<? extends AbstractPackagerBlockEntity> getBlockEntityType() {
        return FactoryBlockEntities.JAR_PACKAGER.get();
    }

    @Override
    protected Class<? extends AbstractPackagerBlockEntity> getBeClass() {
        return JarPackagerBlockEntity.class;
    }

}
