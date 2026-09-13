package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

@Mixin(FilteringRenderer.class)
public class FactoryFluidPanelFilterRendererMixin {
    @WrapOperation(
            method = "renderOnBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueBoxRenderer;renderItemIntoValueBox(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
                    ordinal = 1
            )
    )
    private static void renderFilter(ItemStack filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     Operation<Void> original, @Local(name = "behaviour") FilteringBehaviour behaviour) {
        if (!(behaviour instanceof GenericFilterProvider filterProvider)) {
            original.call(filter, ms, buffer, light, overlay);
            return;
        }

        GenericStack stack = filterProvider.filter();

        GenericContentExtender.registrationOf(stack.key())
                .clientProvider().renderHandler()
                .renderPanelFilter(stack.key(), ms, buffer, light, overlay);
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/filtering/FilteringBehaviour;getFilter()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private static ItemStack disableValueBoxElevation(ItemStack original, @Local FilteringBehaviour behaviour) {
        if (behaviour instanceof GenericFilterProvider filterProvider && !(filterProvider.filter().key() instanceof ItemKey)) {
            original = Items.BUCKET.getDefaultInstance();
        }
        return original;
    }
}
