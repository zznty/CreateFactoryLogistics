package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_logistics.FactoryMenus;
import ru.zznty.create_factory_logistics.logistics.FluidItemStackHandler;

public class FactoryPanelSetFluidMenu extends FactoryPanelSetItemMenu {
    public FactoryPanelSetFluidMenu(MenuType<?> type, int id, Inventory inv, FactoryFluidPanelBehaviour contentHolder) {
        super(type, id, inv, contentHolder);
    }

    public FactoryPanelSetFluidMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public static FactoryPanelSetFluidMenu create(int id, Inventory inv, FactoryFluidPanelBehaviour be) {
        return new FactoryPanelSetFluidMenu(FactoryMenus.FACTORY_FLUID_PANEL_SET_FLUID.get(), id, inv, be);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new FluidItemStackHandler();
    }
}
