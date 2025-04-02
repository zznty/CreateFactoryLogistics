package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientPackageRenderHandler;

public class ItemPackageRenderHandler implements IngredientPackageRenderHandler<ItemIngredientKey> {
    @Override
    public void renderPanelFilter(ItemIngredientKey filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ValueBoxRenderer.renderItemIntoValueBox(filter.stack(), ms, buffer, light, overlay);
    }
}
