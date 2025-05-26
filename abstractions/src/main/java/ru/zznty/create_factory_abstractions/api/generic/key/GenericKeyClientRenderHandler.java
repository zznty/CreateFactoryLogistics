package ru.zznty.create_factory_abstractions.api.generic.key;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface GenericKeyClientRenderHandler<K extends GenericKey> {
    void renderPanelFilter(K key, PoseStack ms, MultiBufferSource buffer, int light, int overlay);
}
