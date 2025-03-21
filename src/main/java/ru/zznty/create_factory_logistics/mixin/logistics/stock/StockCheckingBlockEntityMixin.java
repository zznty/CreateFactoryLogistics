package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientLogisticsManager;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;

import javax.annotation.Nullable;

@Mixin(StockCheckingBlockEntity.class)
public class StockCheckingBlockEntityMixin {
    @Shadow(remap = false)
    public LogisticallyLinkedBehaviour behaviour;

    @Overwrite(remap = false)
    public boolean broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType type, PackageOrderWithCrafts order, @Nullable IdentifiedInventory ignoredHandler, String address) {
        return IngredientLogisticsManager.broadcastPackageRequest(behaviour.freqId, type, IngredientOrder.of(order), ignoredHandler, address);
    }
}
