package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGuiHandler;
import ru.zznty.create_factory_logistics.render.SlotAmountRenderer;

import java.util.List;

public class ItemGuiHandler implements IngredientGuiHandler<ItemIngredientKey> {
    @Override
    public void renderDecorations(GuiGraphics graphics, ItemIngredientKey key, int amount, int x, int y) {
        graphics.renderItemDecorations(Minecraft.getInstance().font, key.stack(), x, y, "");
        SlotAmountRenderer.render(graphics, Minecraft.getInstance().font, x, y, formatValue(amount));
    }

    @Override
    public void renderSlot(GuiGraphics graphics, ItemIngredientKey key, int x, int y) {
        GuiGameElement.of(key.stack())
                .at(x, y)
                .render(graphics);
    }

    @Override
    public LangBuilder nameBuilder(ItemIngredientKey key, int amount) {
        return CreateLang.itemName(key.stack())
                .space()
                .add(CreateLang.text("x" + amount));
    }

    @Override
    public List<Component> tooltipBuilder(ItemIngredientKey key, int amount) {
        List<Component> component = Screen.getTooltipFromItem(Minecraft.getInstance(), key.stack());
        component.add(Component.empty());
        component.add(CreateLang.text("x").add(CreateLang.number(amount)).style(ChatFormatting.GRAY).component());
        return component;
    }

    @Override
    public LangBuilder nameBuilder(ItemIngredientKey key) {
        return CreateLang.itemName(key.stack());
    }

    @Override
    public int stackSize(ItemIngredientKey key) {
        return key.stack().getMaxStackSize();
    }

    @Override
    public int maxStackSize(ItemIngredientKey key) {
        return stackSize(key);
    }

    public static String formatValue(int count) {
        if (count == BigItemStack.INF) return "\u221E";

        return count >= 1000000 ? (count / 1000000) + "m"
                : count >= 10000 ? (count / 1000) + "k"
                : count >= 1000 ? ((count * 10) / 1000) / 10f + "k" : count >= 100 ? count + "" : " " + count;
    }
}
