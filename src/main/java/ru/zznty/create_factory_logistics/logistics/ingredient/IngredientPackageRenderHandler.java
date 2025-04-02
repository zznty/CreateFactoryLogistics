package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IngredientPackageRenderHandler<K extends IngredientKey> {
    void renderPanelFilter(K filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay);
}
