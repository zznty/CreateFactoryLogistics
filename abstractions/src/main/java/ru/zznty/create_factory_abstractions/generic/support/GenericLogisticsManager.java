package ru.zznty.create_factory_abstractions.generic.support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import javax.annotation.Nullable;
import java.util.*;

public final class GenericLogisticsManager {
    private static final Random r = new Random();

    public static boolean broadcastPackageRequest(UUID freqId, LogisticallyLinkedBehaviour.RequestType type,
                                                  GenericOrder order,
                                                  @Nullable IdentifiedInventory ignoredHandler, String address) {
        if (order.isEmpty())
            return false;

        Multimap<GenericPackageTarget, GenericRequest> requests =
                findPackagersForRequest(freqId, order, ignoredHandler, address);

        if (requests.isEmpty())
            return false;

        // Check if targets have accumulated too many packages already
        for (GenericPackageTarget target : requests.keySet())
            if (target.isTooBusyFor(type))
                return false;

        // Actually perform package creation
        performPackageRequests(requests);
        return true;
    }

    public static Multimap<GenericPackageTarget, GenericRequest> findPackagersForRequest(UUID freqId,
                                                                                         GenericOrder order,
                                                                                         @Nullable IdentifiedInventory ignoredHandler,
                                                                                         String address) {
        List<GenericStack> stacks = order.stacks();

        Multimap<GenericPackageTarget, GenericRequest> requests = ArrayListMultimap.create();

        // Packages need to track their index and successors for successful defrag
        Iterable<LogisticallyLinkedBehaviour> availableLinks = LogisticallyLinkedBehaviour.getAllPresent(freqId, true);
        List<LogisticallyLinkedBehaviour> usedLinks = new ArrayList<>();
        MutableBoolean finalLinkTracker = new MutableBoolean(false);

        // First box needs to carry the order specifics for successful defrag
        GenericOrder contextToSend = order;

        // Packages from future orders should not be merged in the packager queue
        int orderId = r.nextInt();

        for (int i = 0; i < stacks.size(); i++) {
            GenericStack stack = stacks.get(i);
            int remainingCount = stack.amount();
            boolean finalEntry = i == stacks.size() - 1;

            for (LogisticallyLinkedBehaviour link : availableLinks) {
                int usedIndex = usedLinks.indexOf(link);
                int linkIndex = usedIndex == -1 ? usedLinks.size() : usedIndex;
                MutableBoolean isFinalLink = new MutableBoolean(false);
                if (linkIndex == usedLinks.size() - 1)
                    isFinalLink = finalLinkTracker;

                LogisticallyLinkedGenericBehaviour ingredientLink = LogisticallyLinkedGenericBehaviour.from(link);

                Pair<GenericPackageTarget, GenericRequest> request = ingredientLink.processRequest(
                        stack.withAmount(remainingCount),
                        address, linkIndex, isFinalLink, orderId, contextToSend, ignoredHandler);
                if (request == null)
                    continue;

                requests.put(request.getFirst(), request.getSecond());

                int processedCount = request.getSecond()
                        .getCount();
                if (processedCount > 0 && usedIndex == -1) {
                    contextToSend = null;
                    usedLinks.add(link);
                    finalLinkTracker = isFinalLink;
                }

                remainingCount -= processedCount;
                if (remainingCount > 0)
                    continue;
                if (finalEntry)
                    finalLinkTracker.setTrue();
                break;
            }
        }
        return requests;
    }

    public static void performPackageRequests(Multimap<GenericPackageTarget, GenericRequest> requests) {
        for (Map.Entry<GenericPackageTarget, Collection<GenericRequest>> entry : requests.asMap().entrySet().stream().toList()) {
            entry.getKey().dispatch(entry.getValue());
        }
    }

    public static int getStockOf(UUID freqId, GenericStack stack,
                                 @Nullable IdentifiedInventory ignoredHandler) {
        int sum = 0;
        for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(freqId, false))
            sum += GenericInventorySummary.of(link.getSummary(ignoredHandler))
                    .getCountOf(stack.key());
        return sum;
    }
}
