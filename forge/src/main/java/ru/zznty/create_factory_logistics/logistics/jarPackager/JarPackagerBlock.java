package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.fluids.FluidUtil;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_logistics.FactoryBlocks;

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
        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockEntity be = context.getLevel()
                    .getBlockEntity(context.getClickedPos()
                            .relative(face));
            if (be instanceof PackagerBlockEntity)
                continue;
            if (be != null && (FluidUtil.getFluidHandler(context.getLevel(), context.getClickedPos()
                            .relative(face), face.getOpposite())
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

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (FactoryBlocks.FACTORY_FLUID_GAUGE.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
