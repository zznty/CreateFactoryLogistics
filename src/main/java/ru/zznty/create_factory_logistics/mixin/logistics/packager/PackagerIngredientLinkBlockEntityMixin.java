package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRequest;
import ru.zznty.create_factory_logistics.logistics.panel.request.PackagerIngredientLinkBlockEntity;

@Mixin(PackagerLinkBlockEntity.class)
public class PackagerIngredientLinkBlockEntityMixin implements PackagerIngredientLinkBlockEntity {

    @Nullable
    @Shadow(remap = false)
    public PackagerBlockEntity getPackager() {
        return null;
    }

    @Override
    public Pair<PackagerBlockEntity, IngredientRequest> processRequest(BoardIngredient ingredient, String address, int linkIndex, MutableBoolean finalLink, int orderId, @Nullable IngredientOrder orderContext, @javax.annotation.Nullable IdentifiedInventory ignoredHandler) {
        PackagerBlockEntity packager = getPackager();
        if (packager == null)
            return null;
        if (packager.isTargetingSameInventory(ignoredHandler))
            return null;

        InventorySummary summary = packager.getAvailableItems();
        int availableCount = ingredient.getCountIn(summary);
        if (availableCount == 0)
            return null;
        int toWithdraw = Math.min(ingredient.amount(), availableCount);
        return Pair.of(packager,
                IngredientRequest.create(ingredient.withAmount(toWithdraw), toWithdraw, address, linkIndex, finalLink, 0, orderId, orderContext));
    }
}
