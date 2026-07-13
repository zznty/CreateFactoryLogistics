package ru.zznty.create_factory_logistics.logistics.panel;

import com.google.common.collect.Multimap;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_abstractions.generic.support.PanelRequestedStacks;
import ru.zznty.create_factory_abstractions.generic.support.StackRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PanelRequestDispatcher {
    private PanelRequestDispatcher() {
    }

    public static boolean dispatch(List<PanelRequestedStacks> toRequest) {
        Map<PanelRequestedStacks, Multimap<PackagerBlockEntity, GenericRequest>> requests = new HashMap<>();

        for (PanelRequestedStacks requestContext : toRequest) {
            for (Map.Entry<UUID, List<StackRequest>> entry : requestContext.ingredients().stream().collect(
                    Collectors.groupingBy(StackRequest::network)).entrySet()) {
                GenericOrder order = GenericOrder.of(requestContext, entry.getValue());
                Multimap<PackagerBlockEntity, GenericRequest> request = GenericLogisticsManager.findPackagersForRequest(
                        entry.getKey(), order, null, requestContext.recipeAddress());

                requests.merge(requestContext, request, (a, b) -> {
                    a.putAll(b);
                    return a;
                });
            }
        }

        for (Multimap<PackagerBlockEntity, GenericRequest> entry : requests.values())
            for (PackagerBlockEntity packager : entry.keySet())
                if (packager.isTooBusyFor(LogisticallyLinkedBehaviour.RequestType.RESTOCK))
                    return false;

        for (Multimap<PackagerBlockEntity, GenericRequest> entry : requests.values())
            GenericLogisticsManager.performPackageRequests(entry);

        for (Map.Entry<PanelRequestedStacks, Multimap<PackagerBlockEntity, GenericRequest>> entry : requests.entrySet()) {
            RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(entry.getKey().resultNetwork());
            if (promises != null)
                promises.add(new RequestPromise(BigGenericStack.of(entry.getKey().result()).asStack()));
        }

        return true;
    }
}
