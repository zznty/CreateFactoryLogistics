package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

@Mixin(PackagerBlockEntity.class)
public class PackagerBlockEntityMixin {
    @Inject(
            method = "unwrapBox",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    private void unwrapOnlyBoxes(ItemStack box, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!box.isEmpty() && box.getItem() instanceof JarPackageItem) {
            cir.setReturnValue(false);
        }
    }
}
