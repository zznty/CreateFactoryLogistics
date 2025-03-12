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
import net.minecraft.client.renderer.Sheets;
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
        /*if (!VisualizationManager.supportsVisualization(entity.level())) {
            ItemStack box = entity.box;
            if (box.isEmpty() || !PackageItem.isPackage(box)) box = AllBlocks.CARDBOARD_BLOCK.asStack();
            PartialModel model = AllPartialModels.PACKAGES.get(ForgeRegistries.ITEMS.getKey(box.getItem()));
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel itemModel = itemRenderer.getModel(box, entity.level(), null, 0);
            itemRenderer.render(box, ItemDisplayContext.FIXED, false, ms, buffer, light, 0, itemModel);
            renderBox(entity, yaw, ms, buffer, light, model);
        }*/

        renderBox(entity, entity.box, yaw, entity.fluidLevel.getValue(pt), ms, buffer, light);
        //renderBox(entity, yaw, ms, buffer, light, FactoryModels.JAR);

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
        sbb.renderInto(ms, buffer.getBuffer(Sheets.translucentCullBlockSheet()));
    }
}
