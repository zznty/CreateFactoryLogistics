package ru.zznty.create_factory_logistics.logistics.jar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import ru.zznty.create_factory_logistics.Config;

import java.util.Optional;

public class JarItemRenderer extends CustomRenderedItemModelRenderer {
    public void render(ItemStack box, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                       ItemDisplayContext displayContext, PoseStack ms, MultiBufferSource buffer, int light,
                       int overlay) {
        renderer.render(model.getOriginalModel(), light);
        if (!JarPackageRenderer.entityRendering) {
            renderFluidContents(box, -1, ms, buffer, light);
        }
    }

    public static void renderFluidContents(ItemStack box, float fluidLevel, PoseStack ms, MultiBufferSource buffer,
                                           int light) {
        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(box);

        if (containedFluid.isEmpty() || containedFluid.get().isEmpty()) return;

        if (fluidLevel < 0)
            fluidLevel = containedFluid.get().getAmount();

        float capHeight = 1 / 16f;
        float tankHullWidth = 1 / 128f;
        float minPuddleHeight = -(1 / 32f);
        float totalHeight = 8f * capHeight - minPuddleHeight;
        float tankWidth = .5f;

        float level = fluidLevel / Config.jarCapacity * totalHeight;

        if (level == 0) return;

        boolean top = containedFluid.get().getFluid()
                .getFluidType()
                .isLighterThanAir();

        float xMin = 0;
        float xMax = xMin + tankWidth - 2 * tankHullWidth;
        float yMin = capHeight + minPuddleHeight - level;
        float yMax = yMin + level;

        if (top) {
            yMin += totalHeight - level;
            yMax += totalHeight - level;
        }

        float zMin = 0;
        float zMax = zMin + tankWidth - 2 * tankHullWidth;

        ms.pushPose();
        TransformStack.of(ms).rotate(Direction.UP.getRotation());
        ms.translate(-xMax / 2, level - totalHeight, -zMax / 2);
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(containedFluid.get(), xMin, yMin,
                                                             zMin, xMax, yMax, zMax,
                                                             buffer, ms, light, false, true);
        ms.popPose();
    }
}
