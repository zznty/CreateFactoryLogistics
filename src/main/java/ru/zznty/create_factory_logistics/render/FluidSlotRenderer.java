package ru.zznty.create_factory_logistics.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class FluidSlotRenderer {
    public static void renderFluidSlot(GuiGraphics instance, int x, int y, FluidStack stack) {
        if (stack.isEmpty() && stack.getRawFluid() != Fluids.EMPTY) {
            stack = new FluidStack(stack.getRawFluid(), 1, stack.getTag());
        }

        var attributes = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(attributes.getStillTexture(stack));

        instance.blit(x + 1, y + 1, 2, 14, 14, sprite);
    }
}
