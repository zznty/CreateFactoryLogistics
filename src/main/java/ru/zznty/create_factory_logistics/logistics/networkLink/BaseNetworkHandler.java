package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Note: class must have scoped lifetime i.e returned as a new instance for each get cap call due to internal caching mechanism
// i'm sure this will cause a lot of bugs but for threshold switches as of right now it's fine
abstract class BaseNetworkHandler {
    protected final UUID network;
    private final NetworkLinkMode mode;
    private List<GenericStack> cachedIngredients = null;

    BaseNetworkHandler(UUID network, NetworkLinkMode mode) {
        this.network = network;
        this.mode = mode;
    }

    protected List<GenericStack> summary() {
        if (cachedIngredients == null) {
            if (mode.includesStored()) {
                GenericInventorySummary summary = GenericInventorySummary.of(
                        LogisticsManager.getSummaryOfNetwork(network, true));
                cachedIngredients = summary.get();
            } else {
                cachedIngredients = new ArrayList<>();
            }

            if (mode.includesPromised()) {
                RequestPromiseQueue queue = Create.LOGISTICS.getQueuedPromises(network);
                if (queue != null)
                    for (RequestPromise promise : queue.flatten(false)) {
                        BigGenericStack stack = BigGenericStack.of(promise.promisedStack);

                        if (!stack.get().isEmpty())
                            cachedIngredients.add(stack.get());
                    }
            }
        }

        return cachedIngredients;
    }
}
