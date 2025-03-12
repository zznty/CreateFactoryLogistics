package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.packager.InventorySummary;
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
import ru.zznty.create_factory_logistics.logistics.panel.request.*;

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
    private void tryRestock() {
    }

    @Shadow(remap = false)
    private void sendEffect(FactoryPanelPosition fromPos, boolean success) {
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

        if (panelBE.restocker) {
            tryRestock();
            return;
        }

        boolean failed = false;

        Multimap<UUID, BigIngredientStack> toRequest = HashMultimap.create();
        List<BigIngredientStack> toRequestAsList = new ArrayList<>();

        for (FactoryPanelConnection connection : targetedBy.values()) {
            FactoryPanelBehaviour source = FactoryPanelBehaviour.at(getWorld(), connection);
            if (source == null)
                return;

            BoardIngredient ingredient = BoardIngredient.of(source);
            InventorySummary summary = LogisticsManager.getSummaryOfNetwork(source.network, true);

            if (ingredient == BoardIngredient.EMPTY || !ingredient.hasEnough(summary)) {
                sendEffect(connection.from, false);
                failed = true;
                continue;
            }

            BigIngredientStack stack = BigIngredientStack.of(ingredient);

            toRequest.put(source.network, stack);
            toRequestAsList.add(stack);
            sendEffect(connection.from, true);
        }

        if (failed)
            return;

        // Input items may come from differing networks
        Map<UUID, Collection<BigIngredientStack>> asMap = toRequest.asMap();
        IngredientOrder requestContext = new IngredientOrder(toRequestAsList);
        List<Multimap<PackagerBlockEntity, IngredientRequest>> requests = new ArrayList<>();

        // Panel may enforce item arrangement
        if (!activeCraftingArrangement.isEmpty())
            requestContext = new IngredientOrder(activeCraftingArrangement.stream()
                    .map(stack -> BigIngredientStack.of(new ItemBoardIngredient(stack)))
                    .toList());

        // Collect request distributions
        for (Map.Entry<UUID, Collection<BigIngredientStack>> entry : asMap.entrySet()) {
            IngredientOrder order = new IngredientOrder(new ArrayList<>(entry.getValue()));
            Multimap<PackagerBlockEntity, IngredientRequest> request =
                    IngredientLogisticsManager.findPackagersForRequest(entry.getKey(), order, requestContext, null, recipeAddress);
            requests.add(request);
        }

        // Check if any packager is busy - cancel all
        for (Multimap<PackagerBlockEntity, IngredientRequest> entry : requests)
            for (PackagerBlockEntity packager : entry.keySet())
                if (packager.isTooBusyFor(LogisticallyLinkedBehaviour.RequestType.RESTOCK))
                    return;

        // Send it
        for (Multimap<PackagerBlockEntity, IngredientRequest> entry : requests)
            IngredientLogisticsManager.performPackageRequests(entry);

        // Keep the output promise
        RequestPromiseQueue promises = Create.LOGISTICS.getQueuedPromises(network);
        if (promises != null)
            promises.add(new RequestPromise(BigIngredientStack.of(BoardIngredient.of((FactoryPanelBehaviour) (Object) this)).asStack()));

        panelBE.advancements.awardPlayer(AllAdvancements.FACTORY_GAUGE);
    }
}
