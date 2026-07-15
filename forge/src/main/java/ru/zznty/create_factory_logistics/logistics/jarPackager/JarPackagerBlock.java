package ru.zznty.create_factory_logistics.logistics.jarPackager;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_abstractions.logistics.packager.AbstractPackagerBlock;
import ru.zznty.create_factory_abstractions.logistics.packager.AbstractPackagerBlockEntity;

public class JarPackagerBlock extends AbstractPackagerBlock {
    public JarPackagerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean prefersBlockEntity(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        return level != null && level.getCapability(Capabilities.FluidHandler.BLOCK, blockEntity.getBlockPos(), null) != null;
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
