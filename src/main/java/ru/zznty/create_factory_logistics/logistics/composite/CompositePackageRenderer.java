package ru.zznty.create_factory_logistics.logistics.composite;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CompositePackageRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType,
                          PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);

        float width = PackageItem.getWidth(stack);

        List<ItemStack> children = CompositePackageItem.getChildren(stack);
        for (int i = 0; i < children.size(); i++) {
            ItemStack child = children.get(i);

            Direction facing = Iterate.horizontalDirections[i % Iterate.horizontalDirections.length];

            ms.pushPose();

            TransformStack.of(ms)
                    .translate(Vec3.atLowerCornerOf(facing.getNormal())
                            .scale(width / 2f + PackageItem.getWidth(child) / 2.5f));

            ms.scale(1.49f, 1.49f, 1.49f);

            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(null, child, ItemDisplayContext.FIXED, false, ms, buffer,
                            Minecraft.getInstance().level, light,
                            overlay, 0);
            ms.popPose();
        }
    }
}
