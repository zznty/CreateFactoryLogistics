package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.List;

@Mixin(StockTickerInteractionHandler.class)
public class StockTickerInteractionHandlerMixin {
    @Redirect(
            method = "interactWithShop",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockTickerBlockEntity;broadcastPackageRequest(Lcom/simibubi/create/content/logistics/packagerLink/LogisticallyLinkedBehaviour$RequestType;Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;Lcom/simibubi/create/content/logistics/packager/IdentifiedInventory;Ljava/lang/String;)Z"
            )
    )
    private static boolean broadcastRequest(StockTickerBlockEntity instance,
                                            LogisticallyLinkedBehaviour.RequestType requestType,
                                            PackageOrder packageOrder, IdentifiedInventory identifiedInventory,
                                            String address) {
        List<GenericStack> stacks = packageOrder.stacks().stream().map(BigGenericStack::of).map(
                BigGenericStack::get).toList();
        GenericOrder order = GenericOrder.order(stacks);
        return GenericLogisticsManager.broadcastPackageRequest(instance.behaviour.freqId, requestType,
                                                               order, identifiedInventory, address);
    }
}
