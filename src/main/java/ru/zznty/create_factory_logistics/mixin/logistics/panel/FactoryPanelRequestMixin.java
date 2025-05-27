package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
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
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import org.joml.Math;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.FactoryCapabilities;
import ru.zznty.create_factory_logistics.compat.extra_gauges.AbstractPanelBehaviourStub;
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
    @Shadow
    public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;

    public FactoryPanelRequestMixin(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
    }

    @Shadow
    public FactoryPanelBlockEntity panelBE() {
        return null;
    }

    @Shadow
    public void resetTimer() {
    }

    @Shadow
    public FactoryPanelPosition getPanelPosition() {
        return null;
    }

    @Shadow
    public RequestPromiseQueue restockerPromises;

    @Shadow
    public int getLevelInStorage() {
        return 0;
    }

    @Shadow
    public int getPromised() {
        return 0;
    }

    @Shadow
    public boolean satisfied, promisedSatisfied, waitingForNetwork, redstonePowered;

    @Shadow
    private int timer, recipeOutput;

    @Shadow
    public String recipeAddress;

    @Shadow
    public List<ItemStack> activeCraftingArrangement;

    @Shadow
    public UUID network;

    @Shadow
    private int getConfigRequestIntervalInTicks() {
        return 0;
    }

    @Shadow
    protected abstract void sendEffect(FactoryPanelPosition fromPos, boolean success);

    @Unique
    private void createFactoryLogistics$sendEffect(FactoryPanelPosition fromPos, FactoryPanelPosition toPos, boolean success) {
        if (getWorld() instanceof ServerLevel serverLevel)
            CatnipServices.NETWORK.sendToClientsAround(serverLevel, getPos(), 64,
                    new FactoryPanelEffectPacket(fromPos, toPos, success));
    }

    @Unique
    private void createFactoryLogistics$tryRestock() {
        FactoryPanelBlockEntity panelBE = panelBE();
        PackagerBlockEntity packager = panelBE.getRestockedPackager();
        if (packager == null)
            return;
        PackagerAttachedHandler handler = packager.getLevel().getCapability(FactoryCapabilities.PACKAGER_ATTACHED, packager.getBlockPos());
        if (handler == null)
            return;

        BoardIngredient ingredient = BoardIngredient.of((FactoryPanelBehaviour) (Object) this);

        IdentifiedInventory identifiedInventory = handler.identifiedInventory();

        if (identifiedInventory == null)
            return;

        int availableOnNetwork = IngredientLogisticsManager.getStockOf(network, ingredient, identifiedInventory);
        if (availableOnNetwork == 0) {
            sendEffect(getPanelPosition(), false);
            return;
        }

        int inStorage = getLevelInStorage();
        int promised = getPromised();
        int demand = ingredient.amount();
        int amountToOrder = java.lang.Math.max(0, demand - promised - inStorage);

        BigIngredientStack orderedIngredient = BigIngredientStack.of(ingredient, java.lang.Math.min(amountToOrder,
                                                                                                    availableOnNetwork));
        IngredientOrder order = IngredientOrder.order(List.of(orderedIngredient));

        sendEffect(getPanelPosition(), true);

        if (!IngredientLogisticsManager.broadcastPackageRequest(network,
                                                                LogisticallyLinkedBehaviour.RequestType.RESTOCK, order,
                                                                identifiedInventory, recipeAddress))
            return;

        restockerPromises.add(new RequestPromise(orderedIngredient.asStack()));
    }

    @Unique
    private boolean createFactoryLogistics$requestDependent(Multimap<UUID, PanelRequestedIngredients> toRequest,
                                                            FactoryPanelConnection sourceConnection,
                                                            FactoryPanelBehaviour context,
                                                            Set<FactoryPanelPosition> visited) {
        FactoryPanelBehaviour source = FactoryPanelBehaviour.at(getWorld(), sourceConnection);
        if (source == null)
            return false;

        if (!visited.add(sourceConnection.from)) {
            // Cycle detected
//            return false;
        }

        BoardIngredient ingredient = BoardIngredient.of(source).withAmount(sourceConnection.amount);
        IngredientInventorySummary summary = (IngredientInventorySummary) LogisticsManager.getSummaryOfNetwork(
                source.network, true);

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

        toRequest.put(source.network, PanelRequestedIngredients.of(source));

        createFactoryLogistics$sendEffect(sourceConnection.from, context.getPanelPosition(), true);
        return true;
    }

    // @Overwrite(remap = false)
    // silent conflicts :D
    @WrapMethod(method = "tickRequests", remap = false)
    private void tickRequests(Operation<Void> original) {
        FactoryPanelBehaviour source = (FactoryPanelBehaviour) (Object) this;

        if (AbstractPanelBehaviourStub.is(source)) {
            // we don't want to override mixins from extra gauges so skip to the original
            original.call();
            return;
        }

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
            toRequest.put(source.network, PanelRequestedIngredients.of(source));
        }

        // Input items may come from differing networks
        Map<PanelRequestedIngredients, Multimap<PackagerBlockEntity, IngredientRequest>> requests = new HashMap<>();

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
                Multimap<PackagerBlockEntity, IngredientRequest> request = IngredientLogisticsManager.findPackagersForRequest(
                        entry.getKey(), order, null, requestedIngredients.recipeAddress());
                if (!request.isEmpty())
                    requests.put(requestedIngredients, request);
            }
        }

        // Check if any packager is busy - cancel all
        for (Multimap<PackagerBlockEntity, IngredientRequest> entry : requests.values())
            for (PackagerBlockEntity packager : entry.keySet())
                if (packager.isTooBusyFor(LogisticallyLinkedBehaviour.RequestType.RESTOCK))
                    return;

        // Send it
        for (Multimap<PackagerBlockEntity, IngredientRequest> entry : requests.values())
            IngredientLogisticsManager.performPackageRequests(entry);

        // Keep the output promises
        RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
        if (promises != null) {
            for (Map.Entry<PanelRequestedIngredients, Multimap<PackagerBlockEntity, IngredientRequest>> entry : requests.entrySet()) {
                // if all requests were sent, add the output promise
                if (entry.getValue().isEmpty())
                    promises.add(new RequestPromise(entry.getKey().result().asStack()));
            }
        }

        panelBE.advancements.awardPlayer(AllAdvancements.FACTORY_GAUGE);
    }

    @ModifyVariable(
            method = "tickStorageMonitor",
            at = @At("STORE"),
            ordinal = 0
    )
    private boolean setSatisfied(boolean value, @Local(ordinal = 1) int promised) {
        return value && promised == 0;
    }
}
