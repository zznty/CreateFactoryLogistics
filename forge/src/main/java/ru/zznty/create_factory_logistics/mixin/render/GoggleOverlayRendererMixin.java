package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GoggleOverlayRenderer.class)
public class GoggleOverlayRendererMixin {
    @Definition(id = "BlockHitResult", type = BlockHitResult.class)
    @Expression("? instanceof BlockHitResult")
    @WrapOperation(
            method = "renderOverlay",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private static boolean instanceOfCheck(Object object, Operation<Boolean> original,
                                           @Local LocalRef<HitResult> hitResult,
                                           @Share("cfl$hitResult") LocalRef<EntityHitResult> result) {
        if (object instanceof EntityHitResult entityResult && entityResult.getType() != HitResult.Type.MISS) {
            result.set(entityResult);
            Vec3 location = entityResult.getLocation();
            hitResult.set(
                    new BlockHitResult(entityResult.getLocation(),
                                       Direction.getNearest(location.x, location.y, location.z),
                                       BlockPos.containing(entityResult.getLocation()), true));

            return true;
        }
        return original.call(object);
    }

    @Definition(id = "IHaveGoggleInformation", type = IHaveGoggleInformation.class)
    @Expression("? instanceof IHaveGoggleInformation")
    @ModifyExpressionValue(
            method = "renderOverlay",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private static boolean blockEntityInstanceOfCheck(boolean original,
                                                      @Share("cfl$hitResult") LocalRef<EntityHitResult> result) {
        if (result.get() != null && result.get().getEntity() instanceof IHaveGoggleInformation) {
            return true;
        }
        return original;
    }

    @Definition(id = "IHaveGoggleInformation", type = IHaveGoggleInformation.class)
    @Expression("(IHaveGoggleInformation) ?")
    @WrapOperation(
            method = "renderOverlay",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private static IHaveGoggleInformation blockEntityCast(Object object, Operation<IHaveGoggleInformation> original,
                                                          @Share("cfl$hitResult") LocalRef<EntityHitResult> result) {
        if (result.get() != null && result.get().getEntity() instanceof IHaveGoggleInformation gte) {
            return gte;
        }
        return original.call(object);
    }
}
