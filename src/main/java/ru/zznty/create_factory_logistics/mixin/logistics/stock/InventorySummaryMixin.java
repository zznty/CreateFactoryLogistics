package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

import java.util.*;

// it wasn't intended to be fluid compatible but who cares kek

@Mixin(InventorySummary.class)
public class InventorySummaryMixin implements IFluidInventorySummary {
    @Unique
    private final Map<Fluid, FluidBoardIngredient> createFactoryLogistics$fluids = new IdentityHashMap<>();

    @Shadow(remap = false)
    private Map<Item, List<BigItemStack>> items = new IdentityHashMap<>();

    @Shadow(remap = false)
    private int totalCount;

    @Shadow(remap = false)
    private List<BigItemStack> stacksByCount;

    @Shadow(remap = false)
    public int contributingLinks;

    @Shadow(remap = false)
    public void add(ItemStack stack, int count) {
    }

    @Shadow(remap = false)
    public int getCountOf(ItemStack stack) {
        return 0;
    }

    @Shadow(remap = false)
    public List<BigItemStack> getStacks() {
        return List.of();
    }

    @Overwrite(remap = false)
    public void add(InventorySummary summary) {
        IFluidInventorySummary otherSummary = (IFluidInventorySummary) summary;
        for (BigIngredientStack stack : otherSummary.get()) {
            add(stack);
        }
        contributingLinks += summary.contributingLinks;
    }

    @Inject(
            method = "copy",
            at = @At("RETURN"),
            remap = false
    )
    private void createFactoryLogistics$copy(CallbackInfoReturnable<InventorySummary> cir) {
        IFluidInventorySummary otherSummary = (IFluidInventorySummary) cir.getReturnValue();
        for (FluidBoardIngredient ingredient : createFactoryLogistics$fluids.values()) {
            if (ingredient.amount() == 0) continue;
            otherSummary.add(BigIngredientStack.of(ingredient));
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
            cir.setReturnValue(getCountOf(liquidBlock.getFluid()));
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
        for (FluidBoardIngredient ingredient : createFactoryLogistics$fluids.values()) {
            stacksByCount.add(BigIngredientStack.of(ingredient).asStack());
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
        for (FluidBoardIngredient ingredient : createFactoryLogistics$fluids.values()) {
            original.add(BigIngredientStack.of(ingredient));
        }
        return original;
    }

    @Overwrite(remap = false)
    public void add(BigItemStack stack) {
        add((BigIngredientStack) stack);
    }

    @Override
    public void add(BigIngredientStack stack) {
        add(stack.getIngredient(), stack.getCount());
    }

    @Override
    public void add(BoardIngredient ingredient, int count) {
        if (ingredient instanceof FluidBoardIngredient fluidIngredient)
            add(fluidIngredient.stack(), count);
        else if (ingredient instanceof ItemBoardIngredient itemIngredient)
            add(itemIngredient.stack(), count);
    }

    @Override
    public void add(FluidStack stack) {
        add(stack, stack.getAmount());
    }

    @Override
    public void add(FluidStack stack, int amount) {
        if (stack.isEmpty() || amount == 0) return;
        boolean existed = createFactoryLogistics$fluids.containsKey(stack.getFluid());
        createFactoryLogistics$fluids.merge(stack.getFluid(), new FluidBoardIngredient(stack, amount),
                (a, b) -> (FluidBoardIngredient) a.withAmount(a.amount() + b.amount()));
        if (!existed)
            totalCount++;
    }

    @Override
    public Collection<FluidBoardIngredient> getFluids() {
        return createFactoryLogistics$fluids.values();
    }

    @Override
    public int getCountOf(Fluid fluid) {
        FluidBoardIngredient ingredient = createFactoryLogistics$fluids.get(fluid);
        return ingredient == null ? 0 : ingredient.amount();
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

    @Override
    public boolean isEmpty() {
        return items.isEmpty() && createFactoryLogistics$fluids.isEmpty();
    }
}
