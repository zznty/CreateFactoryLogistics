package ru.zznty.create_factory_logistics.logistics.ingredient.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientPackageRenderHandler;

public class EmptyPackageRenderHandler implements IngredientPackageRenderHandler<EmptyIngredientKey> {
    @Override
    public void renderPanelFilter(EmptyIngredientKey filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
    }
}
