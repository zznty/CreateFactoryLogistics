package ru.zznty.create_factory_logistics.mixin.logistics.composite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorVisual;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

@Mixin(ChainConveyorVisual.class)
public class CompositePackageChainConveyorVisualMixin {
    @WrapOperation(
            method = "setupBoxVisual",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private @Nullable ResourceLocation getCompositeModelKey(DefaultedRegistry<Item> instance, Object v,
                                                            Operation<ResourceLocation> original,
                                                            @Local(argsOnly = true) ChainConveyorBlockEntity be,
                                                            @Local(argsOnly = true) ChainConveyorPackage box) {
        return original.call(instance, CompositePackageItem.getEffectiveBox(be.getLevel().registryAccess(), box.item).getItem());
    }
}
