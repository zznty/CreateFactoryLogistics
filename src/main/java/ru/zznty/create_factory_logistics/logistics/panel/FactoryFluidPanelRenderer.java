package ru.zznty.create_factory_logistics.logistics.panel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import ru.zznty.create_factory_logistics.FactoryModels;

public class FactoryFluidPanelRenderer extends SmartBlockEntityRenderer<FactoryFluidPanelBlockEntity> {
    public FactoryFluidPanelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FactoryFluidPanelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        for (FactoryPanelBehaviour behaviour : be.panels.values()) {
            if (!behaviour.isActive())
                continue;
            if (behaviour.getAmount() > 0)
                renderBulb(behaviour, partialTicks, ms, buffer, light, overlay);
            for (FactoryPanelConnection connection : behaviour.targetedBy.values())
                FactoryPanelRenderer.renderPath(behaviour, connection, partialTicks, ms, buffer, light, overlay);
            for (FactoryPanelConnection connection : behaviour.targetedByLinks.values())
                FactoryPanelRenderer.renderPath(behaviour, connection, partialTicks, ms, buffer, light, overlay);
        }
    }

    public static void renderBulb(FactoryPanelBehaviour behaviour, float partialTicks, PoseStack ms,
                                  MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = behaviour.blockEntity.getBlockState();

        float xRot = FactoryPanelBlock.getXRot(blockState) + Mth.PI / 2;
        float yRot = FactoryPanelBlock.getYRot(blockState);
        float glow = behaviour.bulb.getValue(partialTicks);

        boolean missingAddress = behaviour.isMissingAddress();
        PartialModel partial = behaviour.redstonePowered || missingAddress ? FactoryModels.FACTORY_FLUID_PANEL_RED_LIGHT
                : FactoryModels.FACTORY_FLUID_PANEL_LIGHT;

        CachedBuffers.partial(partial, blockState)
                .rotateCentered(yRot, Direction.UP)
                .rotateCentered(xRot, Direction.EAST)
                .rotateCentered(Mth.PI, Direction.UP)
                .translate(behaviour.slot.xOffset * .5, 0, behaviour.slot.yOffset * .5)
                .light(glow > 0.125f ? LightTexture.FULL_BRIGHT : light)
                .overlay(overlay)
                .renderInto(ms, buffer.getBuffer(RenderType.translucent()));

        if (glow < .125f)
            return;

        glow = (float) (1 - (2 * Math.pow(glow - .75f, 2)));
        glow = Mth.clamp(glow, -1, 1);
        int color = (int) (200 * glow);

        CachedBuffers.partial(partial, blockState)
                .rotateCentered(yRot, Direction.UP)
                .rotateCentered(xRot, Direction.EAST)
                .rotateCentered(Mth.PI, Direction.UP)
                .translate(behaviour.slot.xOffset * .5, 0, behaviour.slot.yOffset * .5)
                .light(LightTexture.FULL_BRIGHT)
                .color(color, color, color, 255)
                .overlay(overlay)
                .renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
    }
}
