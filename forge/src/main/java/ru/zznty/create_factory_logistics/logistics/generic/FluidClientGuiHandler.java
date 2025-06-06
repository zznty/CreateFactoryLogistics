package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientGuiHandler;
import ru.zznty.create_factory_abstractions.render.SlotAmountRenderer;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.render.FluidSlotRenderer;

import java.util.ArrayList;
import java.util.List;

public class FluidClientGuiHandler implements GenericKeyClientGuiHandler<FluidKey> {
    @Override
    public void renderDecorations(GuiGraphics graphics, FluidKey key, int amount, int x, int y) {
        SlotAmountRenderer.render(graphics, x, y, formatValue(amount));
    }

    @Override
    public void renderSlot(GuiGraphics graphics, FluidKey key, int x, int y) {
        FluidSlotRenderer.renderFluidSlot(graphics, x, y, key.stack());
    }

    @Override
    public LangBuilder nameBuilder(FluidKey key, int amount) {
        return CreateLang.fluidName(key.stack())
                .space()
                .add(FactoryFluidPanelBehaviour.formatLevel(amount));
    }

    @Override
    public List<Component> tooltipBuilder(FluidKey key, int amount) {
        List<Component> list = new ArrayList<>(3);
        list.add(key.stack().getDisplayName());
        list.add(Component.empty());
        list.add(FactoryFluidPanelBehaviour.formatLevel(amount, false).style(ChatFormatting.GRAY).component());
        return list;
    }

    @Override
    public LangBuilder nameBuilder(FluidKey key) {
        return CreateLang.fluidName(key.stack());
    }

    @Override
    public int stackSize(FluidKey key) {
        return 1000;
    }

    @Override
    public int maxStackSize(FluidKey key) {
        return -1;
    }

    public static String formatValue(int count) {
        return FactoryFluidPanelBehaviour.formatLevelShort(count).string();
    }
}
