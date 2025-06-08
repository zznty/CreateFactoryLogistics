package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConfigurationPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FactoryPanelConfigurationPacket.class)
public class FactoryPanelConfigurationPacketMixin {
    @WrapOperation(
            method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBlockEntity;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelConnection;amount:I"
            )
    )
    private void zeroRecipeInputFix(FactoryPanelConnection instance, int value, Operation<Void> original) {
        original.call(instance, Math.max(1, value));
    }
}
