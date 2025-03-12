package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FactoryPanelBehaviour.class)
public class FactoryPanelBehaviourMixin {
    @WrapOperation(
            method = "read",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;recipeOutput:I"
            ),
            remap = false
    )
    private void zeroRecipeOutputFix(FactoryPanelBehaviour instance, int value, Operation<Void> original) {
        original.call(instance, Math.max(1, value));
    }
}
