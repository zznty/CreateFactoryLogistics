package ru.zznty.create_factory_abstractions.generic.key.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientRenderHandler;

public class ItemKeyRenderHandler implements GenericKeyClientRenderHandler<ItemKey> {
    @Override
    public void renderPanelFilter(ItemKey key, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ValueBoxRenderer.renderItemIntoValueBox(key.stack(), ms, buffer, light, overlay);
    }
}
