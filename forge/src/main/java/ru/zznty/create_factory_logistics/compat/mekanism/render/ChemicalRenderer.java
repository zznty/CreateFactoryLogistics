package ru.zznty.create_factory_logistics.compat.mekanism.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.PonderRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import static net.createmod.catnip.render.FluidRenderHelper.renderStillTiledFace;

public class ChemicalRenderer {
    public static void renderChemicalBox(Chemical<?> chemical, PoseStack ms, MultiBufferSource buffer, int light,
                                         float xMin, float yMin, float zMin, float xMax, float yMax,
                                         float zMax) {
        if (chemical.isEmptyType()) return;

        // mekanism does not use alpha channel
        int color = chemical.getTint() | 0xff_000000;

        TextureAtlasSprite texture = MekanismRenderer.getChemicalTexture(chemical);

        VertexConsumer builder = buffer.getBuffer(PonderRenderTypes.fluid());

        int luminosity = (light >> 4) & 0xF;
        light = (light & 0xF00000) | luminosity << 4;

        for (Direction side : Iterate.directions) {
            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis()
                    .isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin,
                                         builder, ms, light, color, texture);
                } else {
                    renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin,
                                         builder, ms, light, color, texture);
                }
            } else {
                renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin,
                                     builder, ms, light, color, texture);
            }
        }
    }
}
