package ru.zznty.create_factory_logistics.mixin.logistics.jar;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

@Mixin(GenericItemEmptying.class)
public class JarDrainMixin {
    @ModifyReturnValue(
            method = "emptyItem",
            at = @At("RETURN"),
            remap = false
    )
    private static Pair<FluidStack, ItemStack> removeEmptyJar(Pair<FluidStack, ItemStack> original, Level world) {
        // prevent item drain leaving empty jars
        if (original.getSecond().getItem() instanceof JarPackageItem &&
                !GenericItemEmptying.canItemBeEmptied(world, original.getSecond()))
            original.setSecond(ItemStack.EMPTY);

        return original;
    }
}
