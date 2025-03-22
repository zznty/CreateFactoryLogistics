package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Pair;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.zznty.create_factory_logistics.logistics.panel.request.*;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

import java.util.UUID;

@Mixin(LogisticallyLinkedBehaviour.class)
public abstract class LogisticallyLinkedIngredientBehaviourMixin extends BlockEntityBehaviour implements LogisticallyLinkedIngredientBehaviour {
    @Shadow(remap = false)
    public UUID freqId;

    public LogisticallyLinkedIngredientBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public Pair<PackagerBlockEntity, IngredientRequest> processRequest(BoardIngredient ingredient, String address, int linkIndex, MutableBoolean finalLink, int orderId, @Nullable IngredientOrder orderContext, @javax.annotation.Nullable IdentifiedInventory ignoredHandler) {
        if (blockEntity instanceof PackagerIngredientLinkBlockEntity plbe)
            return plbe.processRequest(ingredient, address, linkIndex, finalLink, orderId, orderContext,
                    ignoredHandler);

        return null;
    }

    @Override
    public void deductFromAccurateSummary(FluidStack packageContents) {
        IFluidInventorySummary summary = (IFluidInventorySummary) LogisticsManager.ACCURATE_SUMMARIES.getIfPresent(freqId);
        if (summary == null || packageContents.isEmpty())
            return;

        summary.add(packageContents, -Math.min(summary.getCountOf(packageContents.getFluid()), packageContents.getAmount()));
    }
}
