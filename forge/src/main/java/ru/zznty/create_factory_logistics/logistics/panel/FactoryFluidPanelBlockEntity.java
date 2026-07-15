package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_abstractions.logistics.panel.AbstractFactoryPanelBehaviour;
import ru.zznty.create_factory_abstractions.logistics.panel.AbstractFactoryPanelBlockEntity;

public class FactoryFluidPanelBlockEntity extends AbstractFactoryPanelBlockEntity {
    public FactoryFluidPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected AbstractFactoryPanelBehaviour createBehaviour(FactoryPanelBlock.PanelSlot slot) {
        return new FactoryFluidPanelBehaviour(this, slot);
    }

    @Override
    protected ItemStack popPanel(int count) {
        return FactoryBlocks.FACTORY_FLUID_GAUGE.asStack(count - 1);
    }

}
