package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

public record FluidBoardIngredient(FluidStack stack, int amount) implements BoardIngredient {
    @Override
    public boolean hasEnough(InventorySummary summary) {
        return getCountIn(summary) >= amount;
    }

    @Override
    public int getCountIn(InventorySummary summary) {
        IFluidInventorySummary fluidInventorySummary = (IFluidInventorySummary) summary;
        return fluidInventorySummary.getCountOf(stack.getFluid());
    }

    @Override
    public BoardIngredient withAmount(int amount) {
        return new FluidBoardIngredient(stack, amount);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        stack.writeToNBT(tag);
        tag.putInt("Amount", amount);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(2);
        buf.writeFluidStack(stack);
        buf.writeVarInt(amount);
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
