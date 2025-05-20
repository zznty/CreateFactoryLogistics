package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;

public class NetworkLinkBlock extends FaceAttachedHorizontalDirectionalBlock
        implements IBE<NetworkLinkBlockEntity>, ProperWaterloggedBlock, IWrenchable {
    public static final MapCodec<NetworkLinkBlock> CODEC = simpleCodec(NetworkLinkBlock::new);
    public static final String INGREDIENT_TYPE = "ingredient_type";

    public NetworkLinkBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState placed = super.getStateForPlacement(context);
        if (placed == null)
            return null;
        if (placed.getValue(FACE) == AttachFace.CEILING)
            placed = placed.setValue(FACING, placed.getValue(FACING)
                    .getOpposite());
        return withWater(placed, context);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
                                  LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        updateWater(pLevel, pState, pPos);
        return pState;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AllShapes.STOCK_LINK.get(getConnectedDirection(pState));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WATERLOGGED, FACE, FACING));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<NetworkLinkBlockEntity> getBlockEntityClass() {
        return NetworkLinkBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends NetworkLinkBlockEntity> getBlockEntityType() {
        return FactoryBlockEntities.NETWORK_LINK.get();
    }
}
