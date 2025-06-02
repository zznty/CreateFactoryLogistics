package ru.zznty.create_factory_logistics.logistics.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientRenderHandler;

public class FluidClientRenderHandler implements GenericKeyClientRenderHandler<FluidKey> {
    @Override
    public void renderPanelFilter(FluidKey key, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        FluidStack fluid = key.stack();
        FluidRenderer.renderFluidBox(fluid.getFluid(), fluid.getAmount(), -1 / 5f, -1 / 5f, -1 / 32f, 1 / 5f, 1 / 5f, 0,
                                     buffer,
                                     ms, light, true, false, fluid.getTag());
    }
}
