package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ru.zznty.create_factory_logistics.FactoryModels;

public class JarPackagerRenderer extends SmartBlockEntityRenderer<JarPackagerBlockEntity> {
    public JarPackagerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(JarPackagerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        ItemStack renderedBox = be.getRenderedBox();
        float trayOffset = be.getTrayOffset(partialTicks) / 2;
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PackagerBlock.FACING)
                .getOpposite();

        if (facing.getAxis() == Direction.Axis.Y)
            facing = Direction.NORTH;

        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            SuperByteBuffer sbb = CachedBuffers.partial(getTrayModel(blockState), blockState);
            sbb.translate(Vec3.atLowerCornerOf(Direction.DOWN.getNormal())
                            .scale(trayOffset))
                    .rotateCentered(facing.getRotation())
                    .rotateXCenteredDegrees(90)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
        }

        if (!renderedBox.isEmpty()) {
            ms.pushPose();
            var msr = TransformStack.of(ms);
            msr.translate(Vec3.atLowerCornerOf(Direction.DOWN.getNormal())
                            .scale(trayOffset))
                    .translate(.5f, .5f, .5f)
                    .rotateYDegrees(facing.toYRot())
                    .translate(0, 2 / 16f, 2 / 32f * (facing.getAxis() == Direction.Axis.Z ? -1 : 1))
                    .scale(1.49f, 1.49f, 1.49f);
            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(null, renderedBox, ItemDisplayContext.FIXED, false, ms, buffer, be.getLevel(), light,
                            overlay, 0);
            ms.popPose();
        }
    }

    public static PartialModel getTrayModel(BlockState blockState) {
        return FactoryModels.JAR_PACKAGER_TRAY;
    }
}
