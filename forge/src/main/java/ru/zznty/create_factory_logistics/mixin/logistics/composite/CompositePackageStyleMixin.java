package ru.zznty.create_factory_logistics.mixin.logistics.composite;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

@Mixin(PackageItem.class)
public class CompositePackageStyleMixin {
    @Inject(
            method = "getWidth",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void getWidth(ItemStack box, CallbackInfoReturnable<Float> cir) {
        ItemStack effectiveBox = CompositePackageItem.getEffectiveBox(box);
        if (effectiveBox != box) {
            cir.setReturnValue(PackageItem.getWidth(effectiveBox));
        }
    }

    @Inject(
            method = "getHeight",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void getHeight(ItemStack box, CallbackInfoReturnable<Float> cir) {
        ItemStack effectiveBox = CompositePackageItem.getEffectiveBox(box);
        if (effectiveBox != box) {
            cir.setReturnValue(PackageItem.getHeight(effectiveBox));
        }
    }

    @Inject(
            method = "getHookDistance",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void getHookDistance(ItemStack box, CallbackInfoReturnable<Float> cir) {
        ItemStack effectiveBox = CompositePackageItem.getEffectiveBox(box);
        if (effectiveBox != box) {
            cir.setReturnValue(PackageItem.getHookDistance(effectiveBox));
        }
    }
}
