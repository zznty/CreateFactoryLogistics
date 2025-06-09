package ru.zznty.create_factory_logistics.mixin.logistics.jar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

import java.util.Objects;

@Mixin(ChainConveyorBlockEntity.class)
public class ChainConveyorDropMixin {
    @WrapOperation(
            method = "drop",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/box/PackageEntity;fromItemStack(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/box/PackageEntity;"
            ),
            remap = false
    )
    private PackageEntity fromItemStack(Level world, Vec3 position, ItemStack itemstack,
                                        Operation<PackageEntity> original) {
        PackageEntity packageEntity;
        if (itemstack.getItem() instanceof JarPackageItem) {
            packageEntity = FactoryEntities.JAR.get()
                    .create(world);
        } else if (itemstack.getItem() instanceof CompositePackageItem) {
            packageEntity = FactoryEntities.COMPOSITE_PACKAGE.get()
                    .create(world);
        } else {
            packageEntity = original.call(world, position, itemstack);
        }

        Objects.requireNonNull(packageEntity);

        packageEntity.setPos(position);
        packageEntity.setBox(itemstack);
        return packageEntity;
    }
}
