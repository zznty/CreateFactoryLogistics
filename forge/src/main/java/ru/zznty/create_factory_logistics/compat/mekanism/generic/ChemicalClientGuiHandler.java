package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import com.simibubi.create.foundation.utility.CreateLang;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.util.ChemicalUtil;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientGuiHandler;
import ru.zznty.create_factory_abstractions.render.SlotAmountRenderer;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

import java.util.ArrayList;
import java.util.List;

public class ChemicalClientGuiHandler implements GenericKeyClientGuiHandler<ChemicalKey> {
    @Override
    public void renderDecorations(GuiGraphics graphics, ChemicalKey key, int amount, int x, int y) {
        SlotAmountRenderer.render(graphics, x, y, FactoryFluidPanelBehaviour.formatLevelShort(amount).string());
    }

    @Override
    public void renderSlot(GuiGraphics graphics, ChemicalKey key, int x, int y) {
        if (key.chemical().isEmptyType()) return;

        Color tint = new Color(key.chemical().getTint(), false);

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(key.chemical().getIcon());

        graphics.blit(x + 1, y + 1, 2, 14, 14, sprite, tint.getRedAsFloat(), tint.getGreenAsFloat(),
                      tint.getBlueAsFloat(), tint.getAlphaAsFloat());
    }

    @Override
    public LangBuilder nameBuilder(ChemicalKey key, int amount) {
        return nameBuilder(key)
                .space()
                .add(FactoryFluidPanelBehaviour.formatLevel(amount));
    }

    @Override
    public List<Component> tooltipBuilder(ChemicalKey key, int amount) {
        List<Component> list = new ArrayList<>(3);
        list.add(TextComponentUtil.build(key.chemical()));
        ChemicalUtil.addChemicalDataToTooltip(list, key.chemical(), false);
        list.add(Component.empty());
        list.add(FactoryFluidPanelBehaviour.formatLevel(amount, false).style(ChatFormatting.GRAY).component());
        return list;
    }

    @Override
    public LangBuilder nameBuilder(ChemicalKey key) {
        return CreateLang.builder().add(key.chemical().getTextComponent());
    }

    @Override
    public int stackSize(ChemicalKey key) {
        return 1000;
    }

    @Override
    public int maxStackSize(ChemicalKey key) {
        return -1;
    }
}
