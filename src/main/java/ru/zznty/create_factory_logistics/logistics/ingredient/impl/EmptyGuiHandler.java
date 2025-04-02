package ru.zznty.create_factory_logistics.logistics.ingredient.impl;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGuiHandler;

import java.util.List;

public class EmptyGuiHandler implements IngredientGuiHandler<EmptyIngredientKey> {
    @Override
    public void renderDecorations(GuiGraphics graphics, EmptyIngredientKey key, int amount, int x, int y) {
    }

    @Override
    public void renderSlot(GuiGraphics graphics, EmptyIngredientKey key, int x, int y) {
    }

    @Override
    public LangBuilder nameBuilder(EmptyIngredientKey key, int amount) {
        return CreateLang.builder();
    }

    @Override
    public List<Component> tooltipBuilder(EmptyIngredientKey key, int amount) {
        return List.of();
    }

    @Override
    public LangBuilder nameBuilder(EmptyIngredientKey key) {
        return CreateLang.builder();
    }

    @Override
    public int stackSize(EmptyIngredientKey key) {
        return 0;
    }

    @Override
    public int maxStackSize(EmptyIngredientKey key) {
        return 0;
    }
}
