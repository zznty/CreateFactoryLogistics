package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Note: class must have scoped lifetime i.e returned as a new instance for each get cap call due to internal caching mechanism
// i'm sure this will cause a lot of bugs but for threshold switches as of right now it's fine
abstract class BaseNetworkHandler {
    protected final UUID network;
    private final NetworkLinkMode mode;
    private List<BoardIngredient> cachedIngredients = null;

    BaseNetworkHandler(UUID network, NetworkLinkMode mode) {
        this.network = network;
        this.mode = mode;
    }

    protected List<BoardIngredient> summary() {
        if (cachedIngredients == null) {
            if (mode.includesStored()) {
                IngredientInventorySummary summary = (IngredientInventorySummary) LogisticsManager.getSummaryOfNetwork(network, true);
                cachedIngredients = summary.get();
            } else {
                cachedIngredients = new ArrayList<>();
            }

            if (mode.includesPromised()) {
                RequestPromiseQueue queue = Create.LOGISTICS.getQueuedPromises(network);
                if (queue != null)
                    for (RequestPromise promise : queue.flatten(false)) {
                        BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;

                        if (!stack.ingredient().isEmpty())
                            cachedIngredients.add(stack.ingredient());
                    }
            }
        }

        return cachedIngredients;
    }
}
