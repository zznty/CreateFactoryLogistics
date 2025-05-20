package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientPackageRenderHandler;

public class FluidPackageRenderHandler implements IngredientPackageRenderHandler<FluidIngredientKey> {
    @Override
    public void renderPanelFilter(FluidIngredientKey filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        FluidStack fluid = filter.stack();
        /*FluidRenderer.renderFluidBox(fluid.getFluid(), fluid.getAmount(), -1 / 5f, -1 / 5f, -1 / 32f, 1 / 5f, 1 / 5f, 0, buffer, ms, light, true, false, fluid.getTag());*/
        for (Direction side : Iterate.directions) {
            FluidRenderer.renderFluidStream(fluid, side, 1 / 32f, 1f, true, buffer, ms, light);
        }
    }
}
