package ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlocks;
import ru.zznty.create_factory_logistics.logistics.abstractions.panel.AbstractFactoryPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.abstractions.panel.AbstractFactoryPanelBlockEntity;

public class FactoryChemicalPanelBlockEntity extends AbstractFactoryPanelBlockEntity {
    public FactoryChemicalPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected AbstractFactoryPanelBehaviour createBehaviour(FactoryPanelBlock.PanelSlot slot) {
        return new FactoryChemicalPanelBehaviour(this, slot);
    }

    @Override
    protected ItemStack popPanel(int count) {
        return FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE.asStack(count);
    }
}
