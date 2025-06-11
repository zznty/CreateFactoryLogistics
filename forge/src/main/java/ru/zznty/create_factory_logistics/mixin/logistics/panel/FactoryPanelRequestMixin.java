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
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.*;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.compat.extra_gauges.AbstractPanelBehaviourStub;

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
    private void createFactoryLogistics$sendEffect(FactoryPanelPosition fromPos, FactoryPanelPosition toPos,
                                                   boolean success) {
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
        PackagerAttachedHandler handler = PackagerAttachedHandler.get(packager);
        if (handler == null)
            return;

        GenericStack stack = GenericStack.of((FactoryPanelBehaviour) (Object) this);

        IdentifiedInventory identifiedInventory = handler.identifiedInventory();

        if (identifiedInventory == null)
            return;

        int availableOnNetwork = GenericLogisticsManager.getStockOf(network, stack, identifiedInventory);
        if (availableOnNetwork == 0) {
            sendEffect(getPanelPosition(), false);
            return;
        }

        int inStorage = getLevelInStorage();
        int promised = getPromised();
        int demand = stack.amount();
        int amountToOrder = java.lang.Math.max(0, demand - promised - inStorage);

        GenericStack orderedStack = stack.withAmount(java.lang.Math.min(amountToOrder, availableOnNetwork));
        GenericOrder order = GenericOrder.order(List.of(orderedStack));

        sendEffect(getPanelPosition(), true);

        if (!GenericLogisticsManager.broadcastPackageRequest(network, LogisticallyLinkedBehaviour.RequestType.RESTOCK,
                                                             order,
                                                             identifiedInventory, recipeAddress))
            return;

        restockerPromises.add(new RequestPromise(BigGenericStack.of(orderedStack).asStack()));
    }

    @Unique
    private boolean createFactoryLogistics$requestDependent(Multimap<UUID, PanelRequestedStacks> toRequest,
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

        GenericStack ingredient = GenericStack.of(source).withAmount(sourceConnection.amount);
        GenericInventorySummary summary = GenericInventorySummary.of(
                LogisticsManager.getSummaryOfNetwork(source.network, true));

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

        toRequest.put(source.network, PanelRequestedStacks.of(source));

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

        Multimap<UUID, PanelRequestedStacks> toRequest = HashMultimap.create();
        Set<FactoryPanelPosition> visited = new HashSet<>();

        for (FactoryPanelConnection connection : source.targetedBy.values()) {
            if (!createFactoryLogistics$requestDependent(toRequest, connection, source, visited)) {
                sendEffect(connection.from, false);
                return;
            }
        }

        // If all ingredients are present, request main one
        if (visited.size() == targetedBy.size()) {
            toRequest.put(source.network, PanelRequestedStacks.of(source));
        }

        // Input items may come from differing networks
        Map<PanelRequestedStacks, Multimap<PackagerBlockEntity, GenericRequest>> requests = new HashMap<>();

        // Collect request distributions
        for (Map.Entry<UUID, Collection<PanelRequestedStacks>> entry : toRequest.asMap().entrySet()) {
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

            for (PanelRequestedStacks requestedStacks : entry.getValue()) {
                GenericOrder order = GenericOrder.of(requestedStacks);
                Multimap<PackagerBlockEntity, GenericRequest> request = GenericLogisticsManager.findPackagersForRequest(
                        entry.getKey(), order, null, requestedStacks.recipeAddress());
                if (!request.isEmpty())
                    requests.put(requestedStacks, request);
            }
        }

        // Check if any packager is busy - cancel all
        for (Multimap<PackagerBlockEntity, GenericRequest> entry : requests.values())
            for (PackagerBlockEntity packager : entry.keySet())
                if (packager.isTooBusyFor(LogisticallyLinkedBehaviour.RequestType.RESTOCK))
                    return;

        // Send it
        for (Multimap<PackagerBlockEntity, GenericRequest> entry : requests.values())
            GenericLogisticsManager.performPackageRequests(entry);

        // Keep the output promises
        RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
        if (promises != null) {
            for (Map.Entry<PanelRequestedStacks, Multimap<PackagerBlockEntity, GenericRequest>> entry : requests.entrySet()) {
                // if all requests were sent, add the output promise
                if (entry.getValue().isEmpty())
                    promises.add(new RequestPromise(BigGenericStack.of(entry.getKey().result()).asStack()));
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
