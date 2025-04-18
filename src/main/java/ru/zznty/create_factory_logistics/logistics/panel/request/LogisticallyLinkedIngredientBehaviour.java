package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

import javax.annotation.Nullable;

public interface LogisticallyLinkedIngredientBehaviour {
    Pair<PackagerBlockEntity, IngredientRequest> processRequest(BoardIngredient ingredient, String address,
                                                                int linkIndex, MutableBoolean finalLink, int orderId, @Nullable IngredientOrder orderContext,
                                                                @Nullable IdentifiedInventory ignoredHandler);

}
