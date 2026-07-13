package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FactoryPanelRenderer.class)
public class FactoryPanelRendererMixin {

    @Unique
    private static final ThreadLocal<Boolean> cfl$currentConnectionFailed = ThreadLocal.withInitial(() -> false);

    @Inject(method = "renderPath", at = @At("HEAD"))
    private static void cfl$captureConnection(FactoryPanelBehaviour behaviour, FactoryPanelConnection connection,
                                              float partialTicks, PoseStack ms, MultiBufferSource buffer,
                                              int light, int overlay, CallbackInfo ci) {
        cfl$currentConnectionFailed.set(!connection.success);
    }

    @ModifyExpressionValue(
            method = "renderPath",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;satisfied:Z", ordinal = 0),
            require = 1
    )
    private static boolean cfl$overrideSatisfiedForBlink(boolean satisfied) {
        return cfl$currentConnectionFailed.get() ? false : satisfied;
    }

    @ModifyExpressionValue(
            method = "renderPath",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;satisfied:Z", ordinal = 1),
            require = 1
    )
    private static boolean cfl$overrideSatisfiedForOffset(boolean satisfied) {
        return cfl$currentConnectionFailed.get() ? false : satisfied;
    }

    @ModifyExpressionValue(
            method = "renderPath",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;satisfied:Z", ordinal = 2),
            require = 1
    )
    private static boolean cfl$overrideSatisfiedForUVShift(boolean satisfied) {
        return cfl$currentConnectionFailed.get() ? false : satisfied;
    }
}
