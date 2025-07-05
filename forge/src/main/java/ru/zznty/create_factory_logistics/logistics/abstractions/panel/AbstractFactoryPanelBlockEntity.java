package ru.zznty.create_factory_logistics.logistics.abstractions.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public abstract class AbstractFactoryPanelBlockEntity extends FactoryPanelBlockEntity {
    public AbstractFactoryPanelBlockEntity(BlockEntityType<?> type,
                                           BlockPos pos,
                                           BlockState state) {
        super(type, pos, state);
    }

    protected abstract AbstractFactoryPanelBehaviour createBehaviour(FactoryPanelBlock.PanelSlot slot);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        panels = new EnumMap<>(FactoryPanelBlock.PanelSlot.class);
        redraw = true;
        for (FactoryPanelBlock.PanelSlot slot : FactoryPanelBlock.PanelSlot.values()) {
            AbstractFactoryPanelBehaviour e = createBehaviour(slot);
            panels.put(slot, e);
            behaviours.add(e);
        }

        behaviours.add(advancements = new AdvancementBehaviour(this, AllAdvancements.FACTORY_GAUGE));
    }

    @Override
    public void lazyTick() {
        if (level.isClientSide())
            return;

        if (activePanels() == 0)
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());

        if (getType().isValid(getBlockState())) {
            BlockPos pos = worldPosition.relative(FactoryPanelBlock.connectedDirection(getBlockState())
                                                          .getOpposite());
            BlockEntity be = level.getBlockEntity(pos);
            Optional<Block> supportedGauge = be == null ?
                                             Optional.empty() :
                                             PackagerAttachedHandler.get(be).map(
                                                     PackagerAttachedHandler::supportedGauge);
            boolean shouldBeRestocker = supportedGauge.isPresent() && supportedGauge.get() == getBlockState().getBlock();
            if (restocker == shouldBeRestocker)
                return;
            restocker = shouldBeRestocker;
            redraw = true;
            sendData();
        }
    }

    // todo consider restocker on BEs other than PackagerBlockEntity
    @Override
    public @Nullable PackagerBlockEntity getRestockedPackager() {
        BlockState state = getBlockState();
        if (!restocker || !getType().isValid(state))
            return null;
        BlockPos packagerPos = worldPosition.relative(FactoryPanelBlock.connectedDirection(state)
                                                              .getOpposite());
        if (!level.isLoaded(packagerPos))
            return null;
        BlockEntity be = level.getBlockEntity(packagerPos);
        Optional<PackagerAttachedHandler> packagerAttachedHandler = be == null ?
                                                                    Optional.empty() :
                                                                    PackagerAttachedHandler.get(be)
                                                                            .filter(handler -> handler.supportedGauge() == state.getBlock());

        if (packagerAttachedHandler.isEmpty())
            return null;
        if (be instanceof PackagerBlockEntity pbe) {
            return pbe;
        }
        return null;
    }

    protected abstract ItemStack popPanel(int count);

    @Override
    public void destroy() {
        forEachBehaviour(BlockEntityBehaviour::destroy);
        int panelCount = activePanels();
        if (panelCount > 1)
            Block.popResource(level, worldPosition, popPanel(panelCount - 1));
    }
}
