package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class PanelRestocker {
    private final FactoryPanelBehaviour panel;
    private final BiConsumer<FactoryPanelPosition, Boolean> effectSender;

    public PanelRestocker(FactoryPanelBehaviour panel,
                          BiConsumer<FactoryPanelPosition, Boolean> effectSender) {
        this.panel = panel;
        this.effectSender = effectSender;
    }

    public boolean tryRestock(FactoryPanelBlockEntity panelBE, RequestPromiseQueue restockerPromises,
                              UUID network, String recipeAddress) {
        PackagerBlockEntity packager = panelBE.getRestockedPackager();
        if (packager == null)
            return false;
        PackagerAttachedHandler handler = PackagerAttachedHandler.get(packager);
        if (handler == null)
            return false;

        GenericStack stack = GenericStack.of(panel);

        IdentifiedInventory identifiedInventory = handler.identifiedInventory();

        if (identifiedInventory == null)
            return false;

        int availableOnNetwork = GenericLogisticsManager.getStockOf(network, stack, identifiedInventory);
        if (availableOnNetwork == 0) {
            effectSender.accept(panel.getPanelPosition(), false);
            return false;
        }

        int inStorage = panel.getLevelInStorage();
        int promised = panel.getPromised();
        int demand = stack.amount();
        int amountToOrder = Math.max(0, demand - promised - inStorage);

        GenericStack orderedStack = stack.withAmount(Math.min(amountToOrder, availableOnNetwork));
        GenericOrder order = GenericOrder.order(List.of(orderedStack));

        effectSender.accept(panel.getPanelPosition(), true);

        if (!GenericLogisticsManager.broadcastPackageRequest(network, LogisticallyLinkedBehaviour.RequestType.RESTOCK,
                                                             order,
                                                             identifiedInventory, recipeAddress))
            return false;

        restockerPromises.add(new RequestPromise(BigGenericStack.of(orderedStack).asStack()));
        return true;
    }
}
