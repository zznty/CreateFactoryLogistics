package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.content.fluids.FluidMesh;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorVisual;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.transform.Translate;
import dev.engine_room.flywheel.lib.visual.util.SmartRecycler;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

@Mixin(ChainConveyorVisual.class)
public class ChainConveyorVisualMixin {

    @Unique
    private SmartRecycler<TextureAtlasSprite, TransformedInstance> createFactoryLogistics$fluidSurface;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void ctor(VisualizationContext context, ChainConveyorBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        createFactoryLogistics$fluidSurface = new SmartRecycler<>(key ->
                context.instancerProvider()
                        .instancer(InstanceTypes.TRANSFORMED, FluidMesh.surface(key, JarStyles.JAR_WINDOW_WIDTH))
                        .createInstance());
    }

    @Inject(
            method = "beginFrame",
            at = @At("HEAD"),
            remap = false
    )
    private void resetCount(DynamicVisual.Context ctx, CallbackInfo ci) {
        createFactoryLogistics$fluidSurface.resetCount();
    }

    @Inject(
            method = "_delete",
            at = @At("RETURN"),
            remap = false
    )
    private void delete(CallbackInfo ci) {
        createFactoryLogistics$fluidSurface.delete();
    }

    @Inject(
            method = "beginFrame",
            at = @At("RETURN"),
            remap = false
    )
    private void discardExtra(DynamicVisual.Context ctx, CallbackInfo ci) {
        createFactoryLogistics$fluidSurface.discardExtra();
    }

    @Definition(id = "TransformedInstance", type = TransformedInstance.class)
    @Expression("new TransformedInstance[]{?,?}")
    @ModifyExpressionValue(
            method = "setupBoxVisual",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private TransformedInstance[] setupFluidBuffers(TransformedInstance[] original,
                                                    @Local(argsOnly = true) ChainConveyorPackage box,
                                                    @Share("fluidAmount") LocalIntRef fluidAmount) {
        if (!(box.item.getItem() instanceof JarPackageItem)) return original;

        FluidStack fluidStack = FluidUtil.getFluidContained(box.item).orElse(FluidStack.EMPTY);

        if (fluidStack == FluidStack.EMPTY) return original;

        fluidAmount.set(fluidStack.getAmount());

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);

        var atlas = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

        TransformedInstance[] buffers = new TransformedInstance[original.length + Iterate.horizontalDirections.length + 1];
        System.arraycopy(original, 0, buffers, 0, original.length);

        TextureAtlasSprite stillTexture = atlas.apply(clientFluid.getStillTexture(fluidStack));

        for (int i = 0; i < Iterate.horizontalDirections.length + 1; i++) {
            buffers[original.length + i] = createFactoryLogistics$fluidSurface.get(stillTexture);
            buffers[original.length + i].colorArgb(clientFluid.getTintColor(fluidStack));
        }

        return buffers;
    }

    @SuppressWarnings("rawtypes")
    @WrapOperation(
            method = "setupBoxVisual",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/lib/instance/TransformedInstance;uncenter()Ldev/engine_room/flywheel/lib/transform/Translate;"
            ),
            remap = false
    )
    private Translate setupFluidVisual(TransformedInstance instance, Operation<Translate> original,
                                       @Local(ordinal = 0) TransformedInstance rigBuffer,
                                       @Local(ordinal = 1) TransformedInstance boxBuffer,
                                       @Local(ordinal = 2) TransformedInstance buf,
                                       @Share("fluidBufferIndex") LocalIntRef fluidBufferIndex,
                                       @Share("fluidAmount") LocalIntRef fluidAmount) {
        if (buf == rigBuffer || buf == boxBuffer) return original.call(instance);

        var side = fluidBufferIndex.get() >= Iterate.horizontalDirections.length ? Direction.UP : Iterate.horizontalDirections[fluidBufferIndex.get()];
        fluidBufferIndex.set(fluidBufferIndex.get() + 1);

        buf.rotateTo(Direction.UP, side);

        if (side.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            buf.rotateYDegrees(180);

        float mult = side.getAxis() == Direction.Axis.X ? 1 : -1;

        float fillFactor = (float) fluidAmount.get() / JarPackageItem.JAR_CAPACITY;
        float scaleFactor = fillFactor * 4 / 16f / JarStyles.JAR_WINDOW_WIDTH;

        float height = 8 / 16f;

        float center = 5 / 16f / 2f * scaleFactor / 2;

        if (side.getAxis().isHorizontal()) {
            buf.center();
            buf.translateY(8 / 16f - 1 / 128f);
        } else {
            mult = 0;
            buf.translateY(3 / 128f + -4 / 16f * fillFactor);
            buf.scaleZ(1.3f);
            buf.scaleX(1.3f);
        }

        buf.translate(mult * height, 0, -mult * height);

        if (side.getAxis() == Direction.Axis.X)
            buf.scaleX(scaleFactor);
        else if (side.getAxis() == Direction.Axis.Z)
            buf.scaleZ(scaleFactor);

        return instance;
    }
}
