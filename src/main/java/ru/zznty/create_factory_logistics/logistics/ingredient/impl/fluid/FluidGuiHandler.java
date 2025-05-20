package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGuiHandler;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.render.FluidSlotRenderer;
import ru.zznty.create_factory_logistics.render.SlotAmountRenderer;

import java.util.ArrayList;
import java.util.List;

public class FluidGuiHandler implements IngredientGuiHandler<FluidIngredientKey> {
    @Override
    public void renderDecorations(GuiGraphics graphics, FluidIngredientKey key, int amount, int x, int y) {
        SlotAmountRenderer.render(graphics, Minecraft.getInstance().font, x, y, formatValue(amount));
    }

    @Override
    public void renderSlot(GuiGraphics graphics, FluidIngredientKey key, int x, int y) {
        FluidSlotRenderer.renderFluidSlot(graphics, x, y, key.stack());
    }

    @Override
    public LangBuilder nameBuilder(FluidIngredientKey key, int amount) {
        return CreateLang.fluidName(key.stack())
                .space()
                .add(FactoryFluidPanelBehaviour.formatLevel(amount));
    }

    @Override
    public List<Component> tooltipBuilder(FluidIngredientKey key, int amount) {
        List<Component> list = new ArrayList<>(3);
        list.add(key.stack().getHoverName());
        list.add(Component.empty());
        list.add(FactoryFluidPanelBehaviour.formatLevel(amount, false).style(ChatFormatting.GRAY).component());
        return list;
    }

    @Override
    public LangBuilder nameBuilder(FluidIngredientKey key) {
        return CreateLang.fluidName(key.stack());
    }

    @Override
    public int stackSize(FluidIngredientKey key) {
        return 1000;
    }

    @Override
    public int maxStackSize(FluidIngredientKey key) {
        return -1;
    }

    public static String formatValue(int count) {
        return FactoryFluidPanelBehaviour.formatLevelShort(count).string();
    }
}
