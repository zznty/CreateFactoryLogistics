package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.*;
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
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.zznty.create_factory_abstractions.generic.support.PanelRequestedStacks;
import ru.zznty.create_factory_logistics.compat.extra_gauges.AbstractPanelBehaviourStub;
import ru.zznty.create_factory_logistics.logistics.panel.PanelRequestDispatcher;
import ru.zznty.create_factory_logistics.logistics.panel.PanelRequestPlanner;
import ru.zznty.create_factory_logistics.logistics.panel.PanelRestocker;

import java.util.*;

@Debug(export = true)
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
            PanelRestocker restocker = new PanelRestocker(source, this::sendEffect);
            restocker.tryRestock(panelBE, restockerPromises, network, recipeAddress);
            return;
        }

        List<PanelRequestedStacks> toRequest = new ArrayList<>();
        Set<FactoryPanelPosition> visited = new HashSet<>();

        PanelRequestPlanner planner = new PanelRequestPlanner(source, this::createFactoryLogistics$sendEffect);

        for (FactoryPanelConnection connection : source.targetedBy.values()) {
            if (!planner.requestDependent(toRequest, connection, source, visited)) {
                sendEffect(connection.from, false);
                return;
            }
        }

        // If all ingredients are present, request main one
        if (visited.size() == targetedBy.size()) {
            toRequest.add(PanelRequestedStacks.of(source));
        }

        if (!PanelRequestDispatcher.dispatch(toRequest))
            return;

        panelBE.advancements.awardPlayer(AllAdvancements.FACTORY_GAUGE);
    }

    @ModifyVariable(
            method = "tickStorageMonitor",
            at = @At("STORE"),
            name = "shouldSatisfy"
    )
    private boolean setSatisfied(boolean shouldSatisfy, @Local(name = "promised") int promised) {
        return shouldSatisfy && promised == 0;
    }
}
