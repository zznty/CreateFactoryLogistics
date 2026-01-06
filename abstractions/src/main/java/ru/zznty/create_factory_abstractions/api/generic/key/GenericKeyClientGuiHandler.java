package ru.zznty.create_factory_abstractions.api.generic.key;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface GenericKeyClientGuiHandler<K extends GenericKey> {
    void renderDecorations(GuiGraphics graphics, K key, int amount, int x, int y);

    void renderSlot(GuiGraphics graphics, K key, int x, int y);

    LangBuilder nameBuilder(K key, int amount);

    List<Component> tooltipBuilder(K key, int amount);

    LangBuilder nameBuilder(K key);
}
