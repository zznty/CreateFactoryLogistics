package ru.zznty.create_factory_logistics;

import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryPanelSetFluidMenu;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryPanelSetFluidScreen;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

public class FactoryMenus {
    public static final MenuEntry<FactoryPanelSetFluidMenu> FACTORY_FLUID_PANEL_SET_FLUID =
            register("factory_panel_set_fluid", FactoryPanelSetFluidMenu::new, () -> FactoryPanelSetFluidScreen::new);

    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
            String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<MenuBuilder.ScreenFactory<C, S>> screenFactory) {
        return REGISTRATE
                .menu(name, factory, screenFactory)
                .register();
    }

    // Load this class

    public static void register() {
    }
}
