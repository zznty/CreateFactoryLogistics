package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientRenderHandler;

import static ru.zznty.create_factory_logistics.compat.mekanism.render.ChemicalRenderer.renderChemicalBox;

public class ChemicalClientRenderHandler implements GenericKeyClientRenderHandler<ChemicalKey> {
    @Override
    public void renderPanelFilter(ChemicalKey key, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderChemicalBox(key.chemical(), ms, buffer, light, -1 / 5f, -1 / 5f, -1 / 32f, 1 / 5f, 1 / 5f, 0);
    }
}
