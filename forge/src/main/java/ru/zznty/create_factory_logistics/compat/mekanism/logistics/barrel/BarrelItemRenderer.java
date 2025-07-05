package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBehaviour;
import ru.zznty.create_factory_logistics.compat.mekanism.render.ChemicalRenderer;

public class BarrelItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light,
                          int overlay) {
        renderer.render(model.getOriginalModel(), light);
        ms.pushPose();
        ms.translate(0, .11, 0);
        renderChemicalContents(stack, -1, ms, buffer, light);
        ms.popPose();
    }

    public static void renderChemicalContents(ItemStack box, float chemicalLevel, PoseStack ms,
                                              MultiBufferSource buffer,
                                              int light) {
        ChemicalStack<?> containedChemical = FactoryChemicalPanelBehaviour.getChemicalStack(box);

        if (containedChemical.isEmpty()) return;

        if (chemicalLevel < 0)
            chemicalLevel = containedChemical.getAmount();

        float capHeight = 1 / 16f;
        float tankHullWidth = 1 / 128f;
        float minPuddleHeight = -(1 / 32f);
        float totalHeight = 8f * capHeight - minPuddleHeight;
        float tankWidth = .5f;

        float level = chemicalLevel / ChemicalTankTier.BASIC.getStorage() * totalHeight;

        if (level == 0) return;

        float xMin = 0;
        float xMax = xMin + tankWidth - 2 * tankHullWidth;
        float yMin = capHeight + minPuddleHeight - level;
        float yMax = yMin + level;

        float zMin = 0;
        float zMax = zMin + tankWidth - 2 * tankHullWidth;

        ms.pushPose();
        TransformStack.of(ms).rotate(Direction.UP.getRotation());
        ms.translate(-xMax / 2, level - totalHeight, -zMax / 2);
        ChemicalRenderer.renderChemicalBox(containedChemical.getRaw(), ms, buffer, light,
                                           xMin, yMin, zMin, xMax, yMax, zMax);
        ms.popPose();
    }
}
