package ru.zznty.create_factory_abstractions.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_abstractions.api.generic.crafting.OrderProvider;

import static com.simibubi.create.foundation.gui.AllGuiTextures.NUMBERS;

public class SlotAmountRenderer {
    private static void render(Font font, PoseStack ps, float x, float y, String text,
                               boolean largeFont) {
        final float scale = largeFont ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scale;
        final int offset = largeFont ? 0 : -1;

        RenderSystem.disableBlend();
        final int X = (int) ((x + offset + 16f - font.width(text) * scale) * inverseScaleFactor);
        final int Y = (int) ((y + offset + 16f - 8f * scale) * inverseScaleFactor);
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(text, X, Y, 0xffffff, true, ps.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);
        buffer.endBatch();
        RenderSystem.enableBlend();
    }

    public static void render(GuiGraphics graphics, float x, float y,
                              String text) {
        if (Minecraft.getInstance().screen instanceof OrderProvider) {
            renderCreate(graphics, text, x, y);
            return;
        }

        render(graphics, Minecraft.getInstance().font, x, y, text, true);
    }

    public static void render(GuiGraphics graphics, Font font, float x, float y, String text,
                              boolean largeFont) {
        final float scaleFactor = largeFont ? 0.85f : 0.5f;

        var ps = graphics.pose();
        ps.pushPose();
        ps.translate(0, 0, 200);
        ps.scale(scaleFactor, scaleFactor, scaleFactor);

        render(font, ps, x, y, text, largeFont);

        ps.popPose();
    }

    private static void renderCreate(GuiGraphics graphics, String text, float x, float y) {
        var ps = graphics.pose();
        ps.pushPose();
        ps.translate(0, 0, 200);

        blitCreateFont(graphics, text);

        ps.popPose();
    }

    private static void blitCreateFont(GuiGraphics graphics, String text) {
        int x = (int) Math.floor(-text.length() * 2.5);
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));

            if (c == ',')
                continue;

            int index = c - '0';
            int xOffset = index * 6;
            int spriteWidth = NUMBERS.getWidth();

            switch (c) {
                case ' ':
                    x += 4;
                    continue;
                case '.':
                    spriteWidth = 3;
                    xOffset = 60;
                    break;
                case 'k':
                    xOffset = 64;
                    break;
                case 'm':
                    spriteWidth = 7;
                    xOffset = 70;
                    break;
                case 'b':
                    xOffset = 78;
                    break;
                case '\u221E':
                    spriteWidth = 9;
                    xOffset = 84;
                    break;
            }

            RenderSystem.enableBlend();
            graphics.blit(NUMBERS.location, 14 + x, 10, 0, NUMBERS.getStartX() + xOffset, NUMBERS.getStartY(),
                          spriteWidth, NUMBERS.getHeight(), 256, 256);
            x += spriteWidth - 1;
        }
    }
}
