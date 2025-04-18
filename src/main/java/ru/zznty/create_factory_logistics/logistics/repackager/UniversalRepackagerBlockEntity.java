package ru.zznty.create_factory_logistics.logistics.repackager;

import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class UniversalRepackagerBlockEntity extends RepackagerBlockEntity {
    public UniversalRepackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        repackageHelper = new UniversalRepackagerHelper();
    }
}
