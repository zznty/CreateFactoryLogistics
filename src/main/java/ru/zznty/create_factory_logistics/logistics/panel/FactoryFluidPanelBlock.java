package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_logistics.FactoryBlocks;

public class FactoryFluidPanelBlock extends FactoryPanelBlock {
    public FactoryFluidPanelBlock(Properties p_53182_) {
        super(p_53182_);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getBlockEntityClass() {
        return FactoryFluidPanelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FactoryPanelBlockEntity> getBlockEntityType() {
        return FactoryBlockEntities.FACTORY_FLUID_PANEL.get();
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer == null)
            return InteractionResult.PASS;
        ItemStack item = pPlayer.getItemInHand(pHand);
        if (pLevel.isClientSide)
            return InteractionResult.SUCCESS;
        if (!FactoryBlocks.FACTORY_FLUID_GAUGE.isIn(item))
            return InteractionResult.SUCCESS;
        Vec3 location = pHit.getLocation();
        if (location == null)
            return InteractionResult.SUCCESS;

        if (!FactoryPanelBlockItem.isTuned(item)) {
            AllSoundEvents.DENY.playOnServer(pLevel, pPos);
            pPlayer.displayClientMessage(CreateLang.translate("factory_panel.tune_before_placing")
                    .component(), true);
            return InteractionResult.FAIL;
        }

        PanelSlot newSlot = getTargetedSlot(pPos, pState, location);
        withBlockEntityDo(pLevel, pPos, fpbe -> {
            if (!fpbe.addPanel(newSlot, LogisticallyLinkedBlockItem.networkFromStack(FactoryPanelBlockItem.fixCtrlCopiedStack(item))))
                return;
            pPlayer.displayClientMessage(CreateLang.translateDirect("logistically_linked.connected"), true);
            pLevel.playSound(null, pPos, soundType.getPlaceSound(), SoundSource.BLOCKS);
            if (pPlayer.isCreative())
                return;
            item.shrink(1);
            if (item.isEmpty())
                pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        if (pUseContext.isSecondaryUseActive())
            return false;
        if (!FactoryBlocks.FACTORY_FLUID_GAUGE.isIn(pUseContext.getItemInHand()))
            return false;
        Vec3 location = pUseContext.getClickLocation();
        if (location == null)
            return false;

        BlockPos pos = pUseContext.getClickedPos();
        PanelSlot slot = getTargetedSlot(pos, pState, location);
        FactoryPanelBlockEntity blockEntity = getBlockEntity(pUseContext.getLevel(), pos);

        if (blockEntity == null)
            return false;
        if (blockEntity.panels.get(slot)
                .isActive())
            return false;
        return true;
    }
}
