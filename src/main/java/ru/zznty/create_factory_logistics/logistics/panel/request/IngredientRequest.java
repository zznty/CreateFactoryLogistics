package ru.zznty.create_factory_logistics.logistics.panel.request;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;

public record IngredientRequest(BoardIngredient ingredient, MutableInt count, String address, int linkIndex,
                                MutableBoolean finalLink, MutableInt packageCounter, int orderId,
                                @Nullable IngredientOrder context) {

    public static IngredientRequest create(BoardIngredient ingredient, int count, String address, int linkIndex,
                                           MutableBoolean finalLink, int packageCount, int orderId, @Nullable IngredientOrder context) {
        return new IngredientRequest(ingredient, new MutableInt(count), address, linkIndex, finalLink,
                new MutableInt(packageCount), orderId, context);
    }

    public int getCount() {
        return count.intValue();
    }

    public void subtract(int toSubtract) {
        count.setValue(getCount() - toSubtract);
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public static IngredientRequest fromNBT(CompoundTag tag) {
        BoardIngredient ingredient = BoardIngredient.readFromNBT(tag);
        int count = tag.getInt("Count");
        String address = tag.getString("Address");
        int linkIndex = tag.getInt("LinkIndex");
        MutableBoolean finalLink = new MutableBoolean(tag.getBoolean("FinalLink"));
        int packageCount = tag.getInt("PackageCount");
        int orderId = tag.getInt("OrderId");
        IngredientOrder orderContext =
                tag.contains("OrderContext") ? IngredientOrder.read(tag.getCompound("OrderContext")) : null;
        return create(ingredient, count, address, linkIndex, finalLink, packageCount, orderId, orderContext);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Count", count.intValue());
        ingredient.writeToNBT(tag);
        tag.putString("Address", address);
        tag.putInt("LinkIndex", linkIndex);
        tag.putBoolean("FinalLink", finalLink.booleanValue());
        tag.putInt("PackageCount", packageCounter.intValue());
        tag.putInt("OrderId", orderId);
        return tag;
    }
}
