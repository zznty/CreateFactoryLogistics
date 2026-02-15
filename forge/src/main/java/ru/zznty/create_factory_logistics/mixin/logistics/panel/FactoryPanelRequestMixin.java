package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.*;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.compat.extra_gauges.AbstractPanelBehaviourStub;

import java.util.*;
import java.util.stream.Collectors;

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
    private void createFactoryLogistics$sendEffect(FactoryPanelPosition fromPos, FactoryPanelPosition toPos,
                                                   boolean success) {
        AllPackets.sendToNear(getWorld(), getPos(), 64,
                              new FactoryPanelEffectPacket(fromPos, toPos, success));
    }

    @Unique
    private void createFactoryLogistics$tryRestock() {
        FactoryPanelBlockEntity panelBE = panelBE();
        PackagerBlockEntity packager = panelBE.getRestockedPackager();
        if (packager == null)
            return;
        Optional<PackagerAttachedHandler> handler = packager.getCapability(
                AbstractionsCapabilities.PACKAGER_ATTACHED).resolve();
        if (handler.isEmpty())
            return;

        GenericStack stack = GenericStack.of((FactoryPanelBehaviour) (Object) this);

        IdentifiedInventory identifiedInventory = handler.get().identifiedInventory();

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
    private boolean createFactoryLogistics$requestDependent(List<PanelRequestedStacks> toRequest,
                                                            FactoryPanelConnection sourceConnection,
                                                            FactoryPanelBehaviour context,
                                                            Set<FactoryPanelPosition> visited) {
        FactoryPanelBehaviour source = FactoryPanelBehaviour.at(getWorld(), sourceConnection);
        if (source == null)
            return false;

        if (!visited.add(sourceConnection.from)) {
            // Cycle detected
            return source.satisfied || source.promisedSatisfied;
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
            for (FactoryPanelConnection connection : source.targetedBy.values()) {
                visited.remove(connection.from);
            }
        } else {
            return false;
        }

        toRequest.add(PanelRequestedStacks.of(source));

        createFactoryLogistics$sendEffect(sourceConnection.from, context.getPanelPosition(), true);
        return true;
    }

    // @Overwrite(remap = false)
    // silent conflicts :D
    @WrapMethod(method = "tickRequests", remap = false)
    private void tickRequests(Operation<Void> original) {
        FactoryPanelBehaviour source = (FactoryPanelBehaviour) (Object) this;

        if (AbstractPanelBehaviourStub.shouldTick(source)) {
            // we don't want to override mixins from extra gauges so skip to the original
            original.call();
            return;
        }

        FactoryPanelBlockEntity panelBE = panelBE();
        if (targetedBy.isEmpty() && !panelBE.restocker)
            return;
        if (panelBE.restocker)
            restockerPromises.tick();
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

        List<PanelRequestedStacks> toRequest = new ArrayList<>();
        Set<FactoryPanelPosition> visited = new HashSet<>();

        for (FactoryPanelConnection connection : source.targetedBy.values()) {
            if (!createFactoryLogistics$requestDependent(toRequest, connection, source, visited)) {
                sendEffect(connection.from, false);
                return;
            }
        }

        // If all ingredients are present, request main one
        if (visited.size() == targetedBy.size()) {
            toRequest.add(PanelRequestedStacks.of(source));
        }

        // Map of result -> requests of its ingredients
        // Input items may come from differing networks
        Map<PanelRequestedStacks, Multimap<PackagerBlockEntity, GenericRequest>> requests = new LinkedHashMap<>();
        Set<PanelRequestedStacks> contextsWithRequests = new HashSet<>();

        // Collect request distributions
        for (PanelRequestedStacks requestContext : toRequest) {
            // Group ingredients by their source network
            for (Map.Entry<UUID, List<StackRequest>> entry : requestContext.ingredients().stream().collect(
                    Collectors.groupingBy(StackRequest::network)).entrySet()) {
                GenericOrder order = GenericOrder.of(requestContext, entry.getValue());
                Multimap<PackagerBlockEntity, GenericRequest> request = GenericLogisticsManager.findPackagersForRequest(
                        entry.getKey(), order, null, requestContext.recipeAddress());
                if (!request.isEmpty()) {
                    contextsWithRequests.add(requestContext);
                }

                requests.merge(requestContext, request, (a, b) -> {
                    a.putAll(b);
                    return a;
                });
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
        for (PanelRequestedStacks requestContext : toRequest) {
            if (!contextsWithRequests.contains(requestContext)) {
                continue;
            }

            Multimap<PackagerBlockEntity, GenericRequest> request = requests.get(requestContext);
            if (request == null || !request.isEmpty()) {
                continue;
            }

            RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(requestContext.resultNetwork());
            // if all requests were sent, add the output promise
            if (promises != null)
                promises.add(new RequestPromise(BigGenericStack.of(requestContext.result()).asStack()));
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
