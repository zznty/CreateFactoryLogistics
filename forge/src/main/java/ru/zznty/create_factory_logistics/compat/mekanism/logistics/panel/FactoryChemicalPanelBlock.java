package ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlockEntities;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlocks;
import ru.zznty.create_factory_logistics.logistics.abstractions.panel.AbstractFactoryPanelBlock;
import ru.zznty.create_factory_logistics.logistics.abstractions.panel.AbstractFactoryPanelBlockEntity;

public class FactoryChemicalPanelBlock extends AbstractFactoryPanelBlock {
    public FactoryChemicalPanelBlock(Properties p_53182_) {
        super(p_53182_);
    }

    @Override
    public BlockEntityType<? extends FactoryPanelBlockEntity> getBlockEntityType() {
        return FactoryMekanismBlockEntities.FACTORY_CHEMICAL_PANEL.get();
    }

    @Override
    protected Class<? extends AbstractFactoryPanelBlockEntity> getBeClass() {
        return FactoryChemicalPanelBlockEntity.class;
    }

    @Override
    protected BlockEntry<? extends AbstractFactoryPanelBlock> getBlockEntry() {
        return FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE;
    }
}
