package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import net.createmod.catnip.animation.LerpedFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FactoryPanelBehaviour.class)
public class FactoryPanelBulbMixin {

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/animation/LerpedFloat;updateChaseTarget(F)V"),
            require = 1
    )
    private void cascadeOverrideBulbChase(LerpedFloat instance, float target, Operation<Void> original) {
        FactoryPanelBehaviour self = (FactoryPanelBehaviour) (Object) this;
        for (FactoryPanelConnection conn : self.targetedBy.values()) {
            if (!conn.success) {
                target = 0;
                break;
            }
        }
        for (FactoryPanelConnection conn : self.targetedByLinks.values()) {
            if (!conn.success) {
                target = 0;
                break;
            }
        }
        original.call(instance, target);
    }
}
