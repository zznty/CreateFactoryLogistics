package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import javax.annotation.Nullable;
import java.util.*;

public final class IngredientLogisticsManager {
    private static final Random r = new Random();

    public static boolean broadcastPackageRequest(UUID freqId, LogisticallyLinkedBehaviour.RequestType type, IngredientOrder order,
                                                  @Nullable IdentifiedInventory ignoredHandler, String address) {
        if (order.isEmpty())
            return false;

        Multimap<PackagerBlockEntity, IngredientRequest> requests =
                findPackagersForRequest(freqId, order, ignoredHandler, address);

        // Check if packagers have accumulated too many packages already
        for (PackagerBlockEntity packager : requests.keySet())
            if (packager.isTooBusyFor(type))
                return false;

        // Actually perform package creation
        performPackageRequests(requests);
        return true;
    }

    public static Multimap<PackagerBlockEntity, IngredientRequest> findPackagersForRequest(UUID freqId,
                                                                                           IngredientOrder order,
                                                                                           @Nullable IdentifiedInventory ignoredHandler,
                                                                                           String address) {
        List<BigIngredientStack> stacks = order.stacks();

        Multimap<PackagerBlockEntity, IngredientRequest> requests = HashMultimap.create();

        // Packages need to track their index and successors for successful defrag
        Iterable<LogisticallyLinkedBehaviour> availableLinks = LogisticallyLinkedBehaviour.getAllPresent(freqId, true);
        List<LogisticallyLinkedBehaviour> usedLinks = new ArrayList<>();
        MutableBoolean finalLinkTracker = new MutableBoolean(false);

        // First box needs to carry the order specifics for successful defrag
        IngredientOrder contextToSend = order;

        // Packages from future orders should not be merged in the packager queue
        int orderId = r.nextInt();

        for (int i = 0; i < stacks.size(); i++) {
            BigIngredientStack entry = stacks.get(i);
            int remainingCount = entry.getCount();
            boolean finalEntry = i == stacks.size() - 1;
            BoardIngredient requestedIngredient = entry.ingredient();

            for (LogisticallyLinkedBehaviour link : availableLinks) {
                int usedIndex = usedLinks.indexOf(link);
                int linkIndex = usedIndex == -1 ? usedLinks.size() : usedIndex;
                MutableBoolean isFinalLink = new MutableBoolean(false);
                if (linkIndex == usedLinks.size() - 1)
                    isFinalLink = finalLinkTracker;

                LogisticallyLinkedIngredientBehaviour ingredientLink = (LogisticallyLinkedIngredientBehaviour) link;

                Pair<PackagerBlockEntity, IngredientRequest> request = ingredientLink.processRequest(requestedIngredient.withAmount(remainingCount),
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

    public static void performPackageRequests(Multimap<PackagerBlockEntity, IngredientRequest> requests) {
        Map<PackagerBlockEntity, Collection<IngredientRequest>> asMap = requests.asMap();
        for (Map.Entry<PackagerBlockEntity, Collection<IngredientRequest>> entry : asMap.entrySet()) {
            ArrayList<IngredientRequest> queuedRequests = new ArrayList<>(entry.getValue());
            PackagerBlockEntity packager = entry.getKey();

            if (!queuedRequests.isEmpty())
                packager.flashLink();
            for (int i = 0; i < 100; i++) {
                if (queuedRequests.isEmpty())
                    break;

                PackagerIngredientBlockEntity ingredientPackager = (PackagerIngredientBlockEntity) packager;

                ingredientPackager.attemptToSendIngredients(queuedRequests);
            }

            packager.triggerStockCheck();
            packager.notifyUpdate();
        }
    }

    public static int getStockOf(UUID freqId, BoardIngredient ingredient, @Nullable IdentifiedInventory ignoredHandler) {
        int sum = 0;
        for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(freqId, false))
            sum += ((IngredientInventorySummary) link.getSummary(ignoredHandler))
                    .getCountOf(ingredient.key());
        return sum;
    }
}
