package ru.zznty.create_factory_logistics.mixin.bugfixes;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// fixes comparison of held item and simulated reminder
// cuz in base create it only compares item type and count, ignoring nbt and caps
@Mixin(ArmBlockEntity.class)
public class ArmBlockEntityMixin {
    @Redirect(
            method = "collectItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean place1(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }

    @Redirect(
            method = "getDistributableAmount",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean place2(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }
}
