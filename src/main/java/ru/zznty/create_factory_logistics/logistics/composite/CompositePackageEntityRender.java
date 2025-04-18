package ru.zznty.create_factory_logistics.logistics.composite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CompositePackageEntityRender extends EntityRenderer<CompositePackageEntity> {
    public CompositePackageEntityRender(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        shadowRadius = 0.5f;
    }

    @Override
    public void render(CompositePackageEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
        ItemStack box = entity.box;

        if (box.isEmpty() || !PackageItem.isPackage(box)) box = AllBlocks.CARDBOARD_BLOCK.asStack();

        ms.pushPose();

        ms.scale(1.8f, 1.8f, 1.8f);
        ms.translate(0, .3, 0);

        if (entity != null)
            TransformStack.of(ms)
                    .rotate(-AngleHelper.rad(yaw + 90), Direction.UP)
                    .nudge(entity.getId());

        Minecraft.getInstance().getItemRenderer()
                .renderStatic(null, box, ItemDisplayContext.FIXED, false, ms, buffer, null, light,
                        0, 0);

        ms.popPose();

        super.render(entity, yaw, pt, ms, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CompositePackageEntity p_114482_) {
        return null;
    }
}
