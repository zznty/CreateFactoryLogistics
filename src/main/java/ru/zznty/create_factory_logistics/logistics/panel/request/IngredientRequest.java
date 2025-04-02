package ru.zznty.create_factory_logistics.logistics.panel.request;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

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
}
