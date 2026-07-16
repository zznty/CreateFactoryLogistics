package ru.zznty.create_factory_logistics.mixin.logistics.stockLink;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackageTarget;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackagerLinkBlockEntity;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_logistics.logistics.stockLink.StockLinkExternalHandler;

@Mixin(PackagerLinkBlockEntity.class)
public class PackagerIngredientLinkBlockEntityMixin implements GenericPackagerLinkBlockEntity {

    @Nullable
    @Shadow
    public PackagerBlockEntity getPackager() {
        return null;
    }

    /**
     * Resolves an external stock handler (e.g. an AE2 interface) attached to this link.
     * The target sits opposite the link's connected direction, matching
     * {@link PackagerLinkBlockEntity#getPackager}; the queried side is the target's face
     * the link is attached to (the link's connected direction).
     */
    @Nullable
    @Unique
    private StockLinkExternalHandler createFactoryLogistics$externalHandler() {
        PackagerLinkBlockEntity self = (PackagerLinkBlockEntity) (Object) this;
        if (self.getLevel() == null)
            return null;
        Direction connected = PackagerLinkBlock.getConnectedDirection(self.getBlockState());
        return self.getLevel().getCapability(
                StockLinkExternalHandler.CAPABILITY,
                self.getBlockPos().relative(connected.getOpposite()), connected);
    }

    /**
     * Report the external handler's stock through the link so the stock keeper counts it as
     * a linked packager and lists its contents (Create's own path returns EMPTY without a packager).
     */
    @Inject(method = "fetchSummaryFromPackager", at = @At("HEAD"), cancellable = true)
    private void createFactoryLogistics$externalSummary(@Nullable IdentifiedInventory ignoredHandler,
                                                        CallbackInfoReturnable<InventorySummary> cir) {
        StockLinkExternalHandler handler = createFactoryLogistics$externalHandler();
        if (handler != null)
            cir.setReturnValue(handler.getStockSummary().asSummary());
    }

    @Override
    public Pair<GenericPackageTarget, GenericRequest> processRequest(GenericStack stack, String address, int linkIndex,
                                                                     MutableBoolean finalLink, int orderId,
                                                                     @Nullable GenericOrder orderContext,
                                                                     @Nullable IdentifiedInventory ignoredHandler) {
        PackagerLinkBlockEntity self = (PackagerLinkBlockEntity) (Object) this;

        if (self.getLevel() != null) {
            StockLinkExternalHandler handler = createFactoryLogistics$externalHandler();
            if (handler != null) {
                Pair<GenericPackageTarget, GenericRequest> result = handler.handle(
                        self, stack, address, linkIndex, finalLink, orderId, orderContext, ignoredHandler);
                if (result != null)
                    return result;
            }
        }

        PackagerBlockEntity packager = getPackager();
        if (packager == null)
            return null;
        if (packager.isTargetingSameInventory(ignoredHandler))
            return null;

        GenericInventorySummary summary = GenericInventorySummary.of(packager.getAvailableItems());
        int availableCount = summary.getCountOf(stack.key());
        if (availableCount == 0)
            return null;
        int toWithdraw = Math.min(stack.amount(), availableCount);
        return Pair.of(GenericPackageTarget.ofPackager(packager),
                       GenericRequest.create(stack.withAmount(toWithdraw), toWithdraw, address, linkIndex, finalLink, 0,
                                             orderId, orderContext));
    }
}
