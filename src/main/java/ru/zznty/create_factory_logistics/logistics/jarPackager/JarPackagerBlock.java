package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;

public class JarPackagerBlock extends PackagerBlock {
    public JarPackagerBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getBlockEntityClass() {
        return JarPackagerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
        return FactoryBlockEntities.JAR_PACKAGER.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Capability<IFluidHandler> fluidCap = ForgeCapabilities.FLUID_HANDLER;
        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockEntity be = context.getLevel()
                    .getBlockEntity(context.getClickedPos()
                            .relative(face));
            if (be instanceof PackagerBlockEntity)
                continue;
            if (be != null && (be.getCapability(fluidCap)
                    .isPresent())) {
                preferredFacing = face.getOpposite();
                break;
            }
        }

        Player player = context.getPlayer();
        if (preferredFacing == null) {
            Direction facing = context.getNearestLookingDirection();
            preferredFacing = player != null && player
                    .isShiftKeyDown() ? facing : facing.getOpposite();
        }

        if (player != null && !(player instanceof FakePlayer)) {
            if (AllBlocks.PORTABLE_STORAGE_INTERFACE.has(context.getLevel()
                    .getBlockState(context.getClickedPos()
                            .relative(preferredFacing.getOpposite())))) {
                CreateLang.translate("packager.no_portable_storage")
                        .sendStatus(player);
                return null;
            }
        }

        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel()
                        .hasNeighborSignal(context.getClickedPos()))
                .setValue(FACING, preferredFacing);
    }
}
