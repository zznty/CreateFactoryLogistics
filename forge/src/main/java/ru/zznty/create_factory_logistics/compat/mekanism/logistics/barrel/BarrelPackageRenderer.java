package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismModels;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageRenderer;

public class BarrelPackageRenderer extends EntityRenderer<BarrelPackageEntity> {
    public BarrelPackageRenderer(EntityRendererProvider.Context arg) {
        super(arg);
        shadowRadius = .3f;
    }

    @Override
    public void render(BarrelPackageEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
                       int light) {
        renderBox(entity, entity.box, yaw, entity.chemicalLevel.getValue(pt), ms, buffer, light);
        super.render(entity, yaw, pt, ms, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(BarrelPackageEntity arg) {
        return null;
    }

    public static void renderBox(Entity entity, ItemStack box, float yaw, float chemicalLevel, PoseStack ms,
                                 MultiBufferSource buffer, int light) {
        if (box.isEmpty() || !PackageItem.isPackage(box)) box = AllBlocks.CARDBOARD_BLOCK.asStack();

        JarPackageRenderer.renderBox(entity, yaw, ms, buffer, light, FactoryMekanismModels.BARREL);

        ms.pushPose();

        if (entity != null)
            TransformStack.of(ms)
                    .rotate(-AngleHelper.rad(yaw + 90), Direction.UP)
                    .nudge(entity.getId());

        ms.translate(0, .61, 0);
        BarrelItemRenderer.renderChemicalContents(box, chemicalLevel, ms, buffer, light);

        ms.popPose();
    }
}
