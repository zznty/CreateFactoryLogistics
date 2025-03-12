package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlockEntity;

import java.util.EnumMap;
import java.util.List;

public class FactoryFluidPanelBlockEntity extends FactoryPanelBlockEntity {
    public FactoryFluidPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        panels = new EnumMap<>(FactoryPanelBlock.PanelSlot.class);
        redraw = true;
        for (FactoryPanelBlock.PanelSlot slot : FactoryPanelBlock.PanelSlot.values()) {
            FactoryFluidPanelBehaviour e = new FactoryFluidPanelBehaviour(this, slot);
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

        if (FactoryBlocks.FACTORY_FLUID_GAUGE.has(getBlockState())) {
            boolean shouldBeRestocker = FactoryBlocks.JAR_PACKAGER
                    .has(level.getBlockState(worldPosition.relative(FactoryPanelBlock.connectedDirection(getBlockState())
                            .getOpposite())));
            if (restocker == shouldBeRestocker)
                return;
            restocker = shouldBeRestocker;
            redraw = true;
            sendData();
        }
    }

    @Override
    public @Nullable PackagerBlockEntity getRestockedPackager() {
        BlockState state = getBlockState();
        if (!restocker || !FactoryBlocks.FACTORY_FLUID_GAUGE.has(state))
            return null;
        BlockPos packagerPos = worldPosition.relative(FactoryPanelBlock.connectedDirection(state)
                .getOpposite());
        if (!level.isLoaded(packagerPos))
            return null;
        BlockEntity be = level.getBlockEntity(packagerPos);
        if (be instanceof JarPackagerBlockEntity pbe) {
            return pbe;
        }
        return null;
    }

    @Override
    public void destroy() {
        forEachBehaviour(BlockEntityBehaviour::destroy);
        int panelCount = activePanels();
        if (panelCount > 1)
            Block.popResource(level, worldPosition, FactoryBlocks.FACTORY_FLUID_GAUGE.asStack(panelCount - 1));
    }
}
