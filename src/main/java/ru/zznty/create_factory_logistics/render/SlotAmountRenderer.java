package ru.zznty.create_factory_logistics.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_logistics.ClientConfig;

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

    public static void render(GuiGraphics graphics, Font font, float x, float y,
                              String text) {
        if (ClientConfig.fontStyle.equals(ClientConfig.FontStyle.CREATE))
            return; // todo bring back create fonts

        render(graphics, font, x, y, text, ClientConfig.fontStyle == ClientConfig.FontStyle.LARGE);
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
}
