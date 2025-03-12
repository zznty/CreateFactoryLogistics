package ru.zznty.create_factory_logistics.logistics;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FluidItemStackHandler extends ItemStackHandler {
    private Fluid fluid = Fluids.EMPTY;

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (stack.getItem() instanceof BucketItem)
            return true;

        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack resultStack = super.insertItem(slot, stack, simulate);
        if (getStackInSlot(slot).getItem() instanceof BucketItem) {
            return resultStack;
        }

        stacks.set(slot, new ItemStack(fluid.getBucket()));

        return resultStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        fluid = getFluidFromStack(getStackInSlot(slot), fluid);
    }

    public Fluid getFluid() {
        return fluid;
    }

    public static Fluid getFluidFromStack(ItemStack stack, Fluid existingFluid) {
        if (stack.getItem() instanceof BucketItem be) {
            return be.getFluid();
        }

        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(fluidHandler -> {
            if (fluidHandler.getTanks() < 1) return Fluids.EMPTY;
            FluidStack fluidInItem = fluidHandler.getFluidInTank(0);

            if (fluidHandler.getTanks() > 1 && (fluidInItem == FluidStack.EMPTY || fluidInItem.getFluid() == existingFluid)) {
                for (int i = 1; i < fluidHandler.getTanks(); i++) {
                    fluidInItem = fluidHandler.getFluidInTank(i);
                    if (fluidInItem != FluidStack.EMPTY && fluidInItem.getFluid() != existingFluid) {
                        return fluidInItem.getFluid();
                    }
                }
            }

            return fluidInItem.getFluid();
        }).orElse(Fluids.EMPTY);
    }
}
