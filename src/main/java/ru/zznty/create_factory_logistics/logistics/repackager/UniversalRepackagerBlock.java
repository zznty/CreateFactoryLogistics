package ru.zznty.create_factory_logistics.logistics.repackager;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;

public class UniversalRepackagerBlock extends RepackagerBlock {
    public UniversalRepackagerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
        return FactoryBlockEntities.UNIVERSAL_REPACKAGER.get();
    }
}
