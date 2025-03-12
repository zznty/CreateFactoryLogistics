package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

import java.util.*;

// it wasn't intended to be fluid compatible but who cares kek

@Mixin(InventorySummary.class)
public class InventorySummaryMixin implements IFluidInventorySummary {
    @Unique
    private final Map<Fluid, FluidStack> createFactoryLogistics$fluids = new IdentityHashMap<>();

    @Shadow(remap = false)
    private int totalCount;

    @Shadow(remap = false)
    private List<BigItemStack> stacksByCount;

    @Shadow(remap = false)
    public void add(ItemStack stack) {
    }

    @Shadow(remap = false)
    public int getCountOf(ItemStack stack) {
        return 0;
    }

    @Shadow(remap = false)
    public List<BigItemStack> getStacks() {
        return List.of();
    }

    @Inject(
            method = "add(Lcom/simibubi/create/content/logistics/packager/InventorySummary;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void createFactoryLogistics$add(InventorySummary summary, CallbackInfo ci) {
        IFluidInventorySummary otherSummary = (IFluidInventorySummary) summary;
        for (FluidStack stack : otherSummary.getFluids()) {
            if (stack.isEmpty()) continue;
            add(stack);
        }
    }

    @Inject(
            method = "copy",
            at = @At("RETURN"),
            remap = false
    )
    private void createFactoryLogistics$copy(CallbackInfoReturnable<InventorySummary> cir) {
        IFluidInventorySummary otherSummary = (IFluidInventorySummary) cir.getReturnValue();
        for (FluidStack stack : createFactoryLogistics$fluids.values()) {
            if (stack.isEmpty()) continue;
            otherSummary.add(stack);
        }
    }

    @Inject(
            method = "write",
            at = @At("RETURN"),
            remap = false
    )
    private void createFactoryLogistics$write(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().put("FluidList", NBTHelper.writeCompoundList(createFactoryLogistics$fluids.values(), fluidStack -> {
            CompoundTag tag = new CompoundTag();
            fluidStack.writeToNBT(tag);
            return tag;
        }));
    }

    @Inject(
            method = "read",
            at = @At("RETURN"),
            remap = false
    )
    private static void createFactoryLogistics$read(CompoundTag tag, CallbackInfoReturnable<InventorySummary> cir) {
        IFluidInventorySummary summary = (IFluidInventorySummary) cir.getReturnValue();
        NBTHelper.iterateCompoundList(tag.getList("FluidList", CompoundTag.TAG_COMPOUND),
                compoundTag -> summary.add(FluidStack.loadFluidStackFromNBT(compoundTag)));
    }

    @Inject(
            method = "isEmpty",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void createFactoryLogistics$isEmpty(CallbackInfoReturnable<Boolean> cir) {
        // if the original method returns empty, we need to check if our map is empty too
        if (cir.getReturnValueZ())
            cir.setReturnValue(createFactoryLogistics$fluids.isEmpty());
    }

    @Inject(
            method = "getCountOf",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void createFactoryLogistics$getCountOf(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LiquidBlock liquidBlock && cir.getReturnValueI() == 0) {
            int amount = createFactoryLogistics$fluids.getOrDefault(liquidBlock.getFluid(), FluidStack.EMPTY).getAmount();

            cir.setReturnValue(amount);
        }
    }

    @WrapOperation(
            method = "getStacksByCount",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collections;sort(Ljava/util/List;Ljava/util/Comparator;)V"
            ),
            remap = false
    )
    private void getStacksByCount(List<BigItemStack> list, Comparator<BigItemStack> c, Operation<Void> original) {
        for (FluidStack stack : createFactoryLogistics$fluids.values()) {
            stacksByCount.add(BigIngredientStack.of(new FluidBoardIngredient(stack)).asStack());
        }
    }

    @ModifyExpressionValue(
            method = "getStacks",
            at = @At(
                    value = "NEW",
                    target = "()Ljava/util/ArrayList;"
            ),
            remap = false
    )
    private ArrayList<BigIngredientStack> getFluidStacks(ArrayList<BigIngredientStack> original) {
        for (FluidStack stack : createFactoryLogistics$fluids.values()) {
            original.add(BigIngredientStack.of(new FluidBoardIngredient(stack)));
        }
        return original;
    }

    @Overwrite(remap = false)
    public void add(BigItemStack stack) {
        add((BigIngredientStack) stack);
    }

    @Override
    public void add(BigIngredientStack stack) {
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient)
            add(fluidIngredient.stack());
        else if (stack.getIngredient() instanceof ItemBoardIngredient itemIngredient)
            add(itemIngredient.stack());
    }

    @Override
    public void add(FluidStack stack) {
        if (stack.isEmpty()) return;
        boolean existed = createFactoryLogistics$fluids.containsKey(stack.getFluid());
        createFactoryLogistics$fluids.merge(stack.getFluid(), stack, (a, b) -> {
            a = a.copy();
            a.grow(b.getAmount());
            return a;
        });
        if (!existed)
            totalCount++;
    }

    @Override
    public Collection<FluidStack> getFluids() {
        return createFactoryLogistics$fluids.values();
    }

    @Override
    public int getCountOf(Fluid fluid) {
        return createFactoryLogistics$fluids.getOrDefault(fluid, FluidStack.EMPTY).getAmount();
    }

    @Override
    public int getCountOf(BigIngredientStack stack) {
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient)
            return getCountOf(fluidIngredient.stack().getFluid());
        else if (stack.getIngredient() instanceof ItemBoardIngredient itemIngredient)
            return getCountOf(itemIngredient.stack());
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BigIngredientStack> get() {
        return (List<BigIngredientStack>) (Object) getStacks();
    }
}
