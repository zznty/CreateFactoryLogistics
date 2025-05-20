package ru.zznty.create_factory_logistics.mixin.render;

import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltHelper.class)
public class TransportedJarMixin {
    @Inject(
            method = "isItemUpright",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void createFactoryLogistics$isItemUpright(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PackageItem.isPackage(stack)) {
            cir.setReturnValue(false);
        }
    }
}
