package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.compat.packagerspsic.PackagersPSIC;

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
            if (!PackagersPSIC.isInstalled() && AllBlocks.PORTABLE_FLUID_INTERFACE.has(context.getLevel()
                                                                                               .getBlockState(
                                                                                                       context.getClickedPos()
                                                                                                               .relative(
                                                                                                                       preferredFacing.getOpposite())))) {
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
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (player == null)
            return InteractionResult.PASS;

        ItemStack itemInHand = player.getItemInHand(handIn);

        if (FactoryBlocks.FACTORY_FLUID_GAUGE.isIn(itemInHand))
            return InteractionResult.PASS;

        return super.use(state, worldIn, pos, player, handIn, hit);
    }
}
