package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import org.joml.Math;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.FactoryCapabilities;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientLogisticsManager;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRequest;
import ru.zznty.create_factory_logistics.logistics.panel.request.PanelRequestedIngredients;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.*;

@Mixin(FactoryPanelBehaviour.class)
public abstract class FactoryPanelRequestMixin extends FilteringBehaviour implements MenuProvider {
    @Shadow(remap = false)
    public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;

    public FactoryPanelRequestMixin(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
    }

    @Shadow(remap = false)
    public FactoryPanelBlockEntity panelBE() {
        return null;
    }

    @Shadow(remap = false)
    public void resetTimer() {
    }

    @Shadow(remap = false)
    public FactoryPanelPosition getPanelPosition() {
        return null;
    }

    @Shadow(remap = false)
    public RequestPromiseQueue restockerPromises;

    @Shadow(remap = false)
    public int getLevelInStorage() {
        return 0;
    }

    @Shadow(remap = false)
    public int getPromised() {
        return 0;
    }

    @Shadow(remap = false)
    public boolean satisfied, promisedSatisfied, waitingForNetwork, redstonePowered;

    @Shadow(remap = false)
    private int timer, recipeOutput;

    @Shadow(remap = false)
    public String recipeAddress;

    @Shadow(remap = false)
    public List<ItemStack> activeCraftingArrangement;

    @Shadow(remap = false)
    public UUID network;

    @Shadow(remap = false)
    private int getConfigRequestIntervalInTicks() {
        return 0;
    }

    @Shadow
    protected abstract void sendEffect(FactoryPanelPosition fromPos, boolean success);

    @Unique
    private void createFactoryLogistics$sendEffect(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success) {
        AllPackets.sendToNear(getWorld(), getPos(), 64,
                new FactoryPanelEffectPacket(fromPos, toPos, success));
    }

    @Unique
    private void createFactoryLogistics$tryRestock() {
        FactoryPanelBlockEntity panelBE = panelBE();
        PackagerBlockEntity packager = panelBE.getRestockedPackager();
        if (packager == null)
            return;
        Optional<PackagerAttachedHandler> handler = packager.getCapability(FactoryCapabilities.PACKAGER_ATTACHED).resolve();
        if (handler.isEmpty())
            return;

        BoardIngredient ingredient = BoardIngredient.of((FactoryPanelBehaviour) (Object) this);

        IdentifiedInventory identifiedInventory = handler.get().identifiedInventory();

        if (identifiedInventory == null)
            return;

        int availableOnNetwork = IngredientLogisticsManager.getStockOf(network, ingredient, identifiedInventory);
        if (availableOnNetwork == 0) {
            sendEffect(getPanelPosition(), false);
            return;
        }

        int inStorage = getLevelInStorage();
        int promised = getPromised();
        int demand = getAmount();
        int amountToOrder = java.lang.Math.max(0, demand - promised - inStorage);

        BigIngredientStack orderedIngredient = BigIngredientStack.of(ingredient, java.lang.Math.min(amountToOrder, availableOnNetwork));
        IngredientOrder order = IngredientOrder.order(List.of(orderedIngredient));

        sendEffect(getPanelPosition(), true);

        if (!IngredientLogisticsManager.broadcastPackageRequest(network, LogisticallyLinkedBehaviour.RequestType.RESTOCK, order,
                identifiedInventory, recipeAddress))
            return;

        restockerPromises.add(new RequestPromise(orderedIngredient.asStack()));
    }

    @Unique
    private boolean createFactoryLogistics$requestDependent(Multimap<UUID, PanelRequestedIngredients> toRequest, FactoryPanelConnection sourceConnection, FactoryPanelBehaviour context, Set<FactoryPanelPosition> visited) {
        FactoryPanelBehaviour source = FactoryPanelBehaviour.at(getWorld(), sourceConnection);
        if (source == null)
            return false;

        if (!visited.add(sourceConnection.from)) {
            // Cycle detected
//            return false;
        }

        BoardIngredient ingredient = BoardIngredient.of(source);
        IngredientInventorySummary summary = (IngredientInventorySummary) LogisticsManager.getSummaryOfNetwork(source.network, true);

        if (ingredient.isEmpty() || summary.isEmpty()) {
            createFactoryLogistics$sendEffect(sourceConnection.from, context.getPanelPosition(), false);
            return false;
        }

        // Check if we have enough or enough is queued
        if (summary.getCountOf(ingredient.key()) >= ingredient.amount())
            return true;

        if (source.getLevelInStorage() + source.getPromised() >= ingredient.amount()) {
            return false;
        }

        // request lower level ingredients recursively
        if (Config.factoryGaugeCascadeRequest && !source.targetedBy.isEmpty() && !source.recipeAddress.isBlank()) {
            for (FactoryPanelConnection connection : source.targetedBy.values()) {
                if (!createFactoryLogistics$requestDependent(toRequest, connection, source, visited))
                    return false;
            }
        } else {
            return false;
        }

        toRequest.put(source.network, PanelRequestedIngredients.of(getWorld(), sourceConnection.amount, source, source.recipeAddress));

        createFactoryLogistics$sendEffect(sourceConnection.from, context.getPanelPosition(), true);
        return true;
    }

    @Overwrite(remap = false)
    private void tickRequests() {
        FactoryPanelBlockEntity panelBE = panelBE();
        if (targetedBy.isEmpty() && !panelBE.restocker)
            return;
        if (satisfied || promisedSatisfied || waitingForNetwork || redstonePowered)
            return;
        if (timer > 0) {
            timer = Math.min(timer, getConfigRequestIntervalInTicks());
            timer--;
            return;
        }

        resetTimer();

        if (recipeAddress.isBlank())
            return;

        FactoryPanelBehaviour source = (FactoryPanelBehaviour) (Object) this;

        if (panelBE.restocker) {
            createFactoryLogistics$tryRestock();
            return;
        }

        Multimap<UUID, PanelRequestedIngredients> toRequest = HashMultimap.create();
        Set<FactoryPanelPosition> visited = new HashSet<>();

        for (FactoryPanelConnection connection : source.targetedBy.values()) {
            if (!createFactoryLogistics$requestDependent(toRequest, connection, source, visited)) {
                sendEffect(connection.from, false);
                return;
            }
        }

        // If all ingredients are present, request main one
        if (visited.size() == targetedBy.size()) {
            toRequest.put(source.network, PanelRequestedIngredients.of(getWorld(), recipeOutput, source, recipeAddress));
        }

        // Input items may come from differing networks
        List<Multimap<PackagerBlockEntity, IngredientRequest>> requests = new ArrayList<>();

        // Collect request distributions
        for (Map.Entry<UUID, Collection<PanelRequestedIngredients>> entry : toRequest.asMap().entrySet()) {
//            Object2IntMap<PanelRequestedIngredients> craftCounts = new Object2IntOpenHashMap<>();
//
//            // Compute total number of each ingredient to craft
//            for (PanelRequestedIngredients requestedIngredient : entry.getValue()) {
//                if (requestedIngredient.hasCraftingContext())
//                    craftCounts.mergeInt(requestedIngredient, 1, Integer::sum);
//            }
//
//            for (Object2IntMap.Entry<PanelRequestedIngredients> ingredientEntry : craftCounts.object2IntEntrySet()) {
//                Multimap<PackagerBlockEntity, IngredientRequest> request =
//                        IngredientLogisticsManager.findPackagersForRequest(entry.getKey(), IngredientOrder.craftingOrder(ingredientEntry.), null, recipeAddress);
//                requests.add(request);
//            }

            for (PanelRequestedIngredients requestedIngredients : entry.getValue()) {
                IngredientOrder order = IngredientOrder.of(requestedIngredients);
                Multimap<PackagerBlockEntity, IngredientRequest> request = IngredientLogisticsManager.findPackagersForRequest(entry.getKey(), order, null, requestedIngredients.recipeAddress());
                requests.add(request);
            }
        }

        // Check if any packager is busy - cancel all
        for (Multimap<PackagerBlockEntity, IngredientRequest> entry : requests)
            for (PackagerBlockEntity packager : entry.keySet())
                if (packager.isTooBusyFor(LogisticallyLinkedBehaviour.RequestType.RESTOCK))
                    return;

        // Send it
        for (Multimap<PackagerBlockEntity, IngredientRequest> entry : requests)
            IngredientLogisticsManager.performPackageRequests(entry);

        // Keep the output promises
        RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
        if (promises != null) {
            for (PanelRequestedIngredients requestedIngredients : toRequest.values()) {
                promises.add(new RequestPromise(requestedIngredients.result().asStack()));
            }
        }

        panelBE.advancements.awardPlayer(AllAdvancements.FACTORY_GAUGE);
    }

    @ModifyVariable(
            method = "tickStorageMonitor",
            at = @At("STORE"),
            ordinal = 0,
            remap = false
    )
    private boolean setSatisfied(boolean value, @Local(ordinal = 1) int promised) {
        return value && promised == 0;
    }
}
