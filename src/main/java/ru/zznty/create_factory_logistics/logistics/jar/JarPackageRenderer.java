package ru.zznty.create_factory_logistics.logistics.jar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import ru.zznty.create_factory_logistics.FactoryModels;

public class JarPackageRenderer extends EntityRenderer<JarPackageEntity> {
    public static boolean entityRendering = false;

    public JarPackageRenderer(Context pContext) {
        super(pContext);
        shadowRadius = 0.3f;
    }

    @Override
    public void render(JarPackageEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
//        if (!VisualizationManager.supportsVisualization(entity.level()))
        renderBox(entity, entity.box, yaw, entity.fluidLevel.getValue(pt), ms, buffer, light);

        super.render(entity, yaw, pt, ms, buffer, light);
    }

    public static void renderBox(Entity entity, ItemStack box, float yaw, float fluidLevel, PoseStack ms, MultiBufferSource buffer, int light) {
        if (box.isEmpty() || !PackageItem.isPackage(box)) box = AllBlocks.CARDBOARD_BLOCK.asStack();

        renderBox(entity, yaw, ms, buffer, light, FactoryModels.JAR);

        ms.pushPose();

        if (entity != null)
            TransformStack.of(ms)
                    .rotate(-AngleHelper.rad(yaw + 90), Direction.UP)
                    .nudge(entity.getId());

        ms.translate(0, .61, 0);
        JarItemRenderer.renderFluidContents(box, fluidLevel, ms, buffer, light);

        ms.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(JarPackageEntity p_114482_) {
        return null;
    }

    private static void renderBox(Entity entity, float yaw, PoseStack ms, MultiBufferSource buffer, int light,
                                  PartialModel model) {
        if (model == null)
            return;
        SuperByteBuffer sbb = CachedBuffers.partial(model, Blocks.AIR.defaultBlockState());
        sbb.translate(-.5, .02, -.5)
                .rotateCentered(-AngleHelper.rad(yaw + 90), Direction.UP)
                .light(light);
        if (entity != null)
            sbb.nudge(entity.getId());
        sbb.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
    }
}
