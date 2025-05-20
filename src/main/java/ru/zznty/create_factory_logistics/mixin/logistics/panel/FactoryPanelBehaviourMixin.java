package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientFilterProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGui;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientPromiseQueue;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.Map;
import java.util.UUID;

@Mixin(FactoryPanelBehaviour.class)
public class FactoryPanelBehaviourMixin extends FilteringBehaviour implements IngredientFilterProvider {
    @Shadow
    private int lastReportedLevelInStorage, lastReportedPromises;
    @Shadow
    public boolean forceClearPromises, satisfied, waitingForNetwork, redstonePowered;
    @Shadow
    public RequestPromiseQueue restockerPromises;
    @Shadow
    public UUID network;
    @Shadow
    public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;

    @Shadow
    private InventorySummary getRelevantSummary() {
        return null;
    }

    @Shadow
    public FactoryPanelBlockEntity panelBE() {
        return null;
    }

    @Shadow
    private int getPromiseExpiryTimeInTicks() {
        return 0;
    }

    @Shadow
    public void resetTimerSlightly() {
    }

    @Shadow
    public boolean isMissingAddress() {
        return false;
    }

    public FactoryPanelBehaviourMixin(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;recipeOutput:I"
            )
    )
    private void zeroRecipeOutputFix(FactoryPanelBehaviour instance, int value, Operation<Void> original) {
        original.call(instance, Math.max(1, value));
    }

    @Overwrite
    public int getLevelInStorage() {
        if (blockEntity.isVirtual())
            return 1;
        if (getWorld().isClientSide())
            return lastReportedLevelInStorage;

        BoardIngredient ingredient = BoardIngredient.of((FactoryPanelBehaviour) (Object) this);

        if (ingredient.key() == IngredientKey.EMPTY)
            return 0;

        IngredientInventorySummary summary = (IngredientInventorySummary) getRelevantSummary();
        return summary.getCountOf(ingredient.key());
    }

    @Overwrite
    public int getPromised() {
        if (getWorld().isClientSide())
            return lastReportedPromises;

        BoardIngredient ingredient = BoardIngredient.of((FactoryPanelBehaviour) (Object) this);

        if (ingredient.key() == IngredientKey.EMPTY)
            return 0;

        if (panelBE().restocker) {
            IngredientPromiseQueue restockerPromiseQueue = (IngredientPromiseQueue) restockerPromises;

            if (forceClearPromises) {

                restockerPromiseQueue.forceClear(ingredient);

                resetTimerSlightly();
            }
            forceClearPromises = false;
            return restockerPromiseQueue.getTotalPromisedAndRemoveExpired(ingredient, getPromiseExpiryTimeInTicks());
        }

        IngredientPromiseQueue promises = (IngredientPromiseQueue) Create.LOGISTICS.getQueuedPromises(network);
        if (promises == null)
            return 0;

        if (forceClearPromises) {
            promises.forceClear(ingredient);
            resetTimerSlightly();
        }
        forceClearPromises = false;

        return promises.getTotalPromisedAndRemoveExpired(ingredient, getPromiseExpiryTimeInTicks());
    }

    @Overwrite
    public MutableComponent getLabel() {
        if (!targetedBy.isEmpty() && count == 0 && satisfied) {
            return CreateLang.translate("gui.factory_panel.no_target_amount_set")
                    .style(ChatFormatting.RED)
                    .component();
        }

        if (isMissingAddress()) {
            return CreateLang.translate("gui.factory_panel.address_missing")
                    .style(ChatFormatting.RED)
                    .component();
        }

        BoardIngredient ingredient = BoardIngredient.of((FactoryPanelBehaviour) (Object) this);

        LangBuilder result;
        // we dont want to compare count here
        if (ingredient.key() == IngredientKey.EMPTY)
            result = CreateLang.translate("factory_panel.new_factory_task");
        else if (waitingForNetwork)
            result = CreateLang.translate("factory_panel.some_links_unloaded");
        else if ((getAmount() == 0 || targetedBy.isEmpty()) && satisfied)
            result = IngredientGui.nameBuilder(ingredient.key());
        else {
            result = IngredientGui.nameBuilder(ingredient.key());
            if (redstonePowered)
                result.space()
                        .add(CreateLang.translate("factory_panel.redstone_paused"));
            else if (!satisfied)
                result.space()
                        .add(CreateLang.translate("factory_panel.in_progress"));
        }

        return result.component();
    }

    @Override
    public BoardIngredient ingredient() {
        return new BoardIngredient(IngredientKey.of(getFilter()), count * (upTo ? 1 : getFilter().getMaxStackSize()));
    }
}
