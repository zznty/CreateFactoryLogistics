package ru.zznty.create_factory_logistics.render;

import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidSlotRenderer {
    public static void renderFluidSlot(GuiGraphics instance, int x, int y, FluidStack stack) {
        if (stack.isEmpty() && stack.getFluid() != Fluids.EMPTY) {
            stack = new FluidStack(stack.getFluidHolder(), 1, stack.getComponents().copy().asPatch());
        }

        var attributes = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(attributes.getStillTexture(stack));

        Color tint = new Color(attributes.getTintColor(stack), true);

        instance.blit(x + 1, y + 1, 2, 14, 14, sprite, tint.getRedAsFloat(), tint.getGreenAsFloat(), tint.getBlueAsFloat(), tint.getAlphaAsFloat());
    }
}
