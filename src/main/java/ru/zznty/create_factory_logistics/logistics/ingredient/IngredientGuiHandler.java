package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IngredientGuiHandler<K extends IngredientKey> {
    void renderDecorations(GuiGraphics graphics, K key, int amount, int x, int y);

    void renderSlot(GuiGraphics graphics, K key, int x, int y);

    LangBuilder nameBuilder(K key, int amount);

    List<Component> tooltipBuilder(K key, int amount);

    LangBuilder nameBuilder(K key);

    /**
     * Returns stack (shift-click) size of a key
     * Note: it is here because I don't feel it would be useful anywhere except client gui
     */
    int stackSize(K key);

    /**
     * Returns stack (shift-click) size of a key
     * Note: it is here because I don't feel it would be useful anywhere except client gui
     * Return negative value if size is unlimited e.g. fluids
     */
    int maxStackSize(K key);
}
