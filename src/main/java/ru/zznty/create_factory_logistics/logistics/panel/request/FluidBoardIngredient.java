package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

public record FluidBoardIngredient(FluidStack stack) implements BoardIngredient {
    @Override
    public int amount() {
        return stack.getAmount();
    }

    @Override
    public boolean hasEnough(InventorySummary summary) {
        return getCountIn(summary) >= stack.getAmount();
    }

    @Override
    public int getCountIn(InventorySummary summary) {
        IFluidInventorySummary fluidInventorySummary = (IFluidInventorySummary) summary;
        return fluidInventorySummary.getCountOf(stack.getFluid());
    }

    @Override
    public BoardIngredient withAmount(int amount) {
        FluidStack stackCopy = stack.copy();
        stackCopy.setAmount(amount);
        return new FluidBoardIngredient(stackCopy);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        stack.writeToNBT(tag);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(2);
        buf.writeFluidStack(stack);
    }

    @Override
    public boolean canStack(BoardIngredient ingredient) {
        if (ingredient instanceof FluidBoardIngredient fluidBoardIngredient) {
            return stack.isFluidEqual(fluidBoardIngredient.stack);
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluidBoardIngredient fluidBoardIngredient && fluidBoardIngredient.stack.equals(stack);
    }

    @Override
    public int hashCode() {
        return stack.hashCode();
    }
}
