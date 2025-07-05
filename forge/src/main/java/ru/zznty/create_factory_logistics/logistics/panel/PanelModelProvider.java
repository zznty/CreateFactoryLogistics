package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.ItemStack;

public interface PanelModelProvider {
    BlockEntry<? extends FactoryPanelBlock> model();

    ItemStack defaultPackage();
}
