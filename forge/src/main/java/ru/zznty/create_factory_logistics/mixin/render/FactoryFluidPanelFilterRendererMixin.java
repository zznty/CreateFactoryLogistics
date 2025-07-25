package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

@Mixin(FilteringRenderer.class)
public class FactoryFluidPanelFilterRendererMixin {
    @WrapOperation(
            method = "renderOnBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueBoxRenderer;renderItemIntoValueBox(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
                    ordinal = 1
            ),
            remap = false
    )
    private static void renderFilter(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     Operation<Void> original, @Local FilteringBehaviour behaviour) {
        if (behaviour instanceof FactoryPanelBehaviour panelBehaviour) {
            GenericStack stack = GenericStack.of(panelBehaviour);
            GenericContentExtender.registrationOf(stack.key())
                    .clientProvider().renderHandler()
                    .renderPanelFilter(stack.key(), ms, buffer, light, overlay);
            return;
        }

        original.call(filter, ms, buffer, light, overlay);
    }
}
