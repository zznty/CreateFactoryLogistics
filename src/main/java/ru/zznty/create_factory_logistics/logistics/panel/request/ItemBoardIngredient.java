package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public record ItemBoardIngredient(ItemStack stack, int amount) implements BoardIngredient {
    @Override
    public boolean hasEnough(InventorySummary summary) {
        return summary.getCountOf(stack) >= stack.getCount();
    }

    @Override
    public int getCountIn(InventorySummary summary) {
        return summary.getCountOf(stack);
    }

    @Override
    public BoardIngredient withAmount(int amount) {
        return new ItemBoardIngredient(stack, amount);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.put("Item", stack.save(new CompoundTag()));
        tag.putInt("Amount", amount());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(1);
        buf.writeItem(stack);
        buf.writeVarInt(amount);
    }

    @Override
    public boolean canStack(BoardIngredient ingredient) {
        if (ingredient instanceof ItemBoardIngredient itemIngredient) {
            return ItemHandlerHelper.canItemStacksStack(stack, itemIngredient.stack);
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemBoardIngredient fluidBoardIngredient && fluidBoardIngredient.stack.equals(stack, true);
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(stack.getItem());
        builder.append(amount());
        CompoundTag shareTag = stack.getShareTag();
        if (shareTag != null)
            builder.append(shareTag);
        return builder.hashCode();
    }
}
