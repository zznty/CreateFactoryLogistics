package ru.zznty.create_factory_logistics.mixin.logistics.composite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

@Mixin(ChainConveyorRenderer.class)
public class CompositePackageChainConveyorRendererMixin {
    @WrapOperation(
            method = "renderBox",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private @Nullable ResourceLocation getCompositeModelKey(DefaultedRegistry instance, Object t, Operation<ResourceLocation> original, @Local(argsOnly = true) ChainConveyorPackage box, @Local(argsOnly = true) ChainConveyorBlockEntity be) {
        return original.call(instance, CompositePackageItem.getEffectiveBox(be.getLevel().registryAccess(), box.item).getItem());
    }
}
