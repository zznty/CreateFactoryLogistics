package ru.zznty.create_factory_logistics.mixin.logistics.stockLink;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackagerLinkBlockEntity;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;

@Mixin(PackagerLinkBlockEntity.class)
public class PackagerIngredientLinkBlockEntityMixin implements GenericPackagerLinkBlockEntity {

    @Nullable
    @Shadow
    public PackagerBlockEntity getPackager() {
        return null;
    }

    @Override
    public Pair<PackagerBlockEntity, GenericRequest> processRequest(GenericStack stack, String address, int linkIndex,
                                                                    MutableBoolean finalLink, int orderId,
                                                                    @Nullable GenericOrder orderContext,
                                                                    @Nullable IdentifiedInventory ignoredHandler) {
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
        return Pair.of(packager,
                       GenericRequest.create(stack.withAmount(toWithdraw), toWithdraw, address, linkIndex, finalLink, 0,
                                             orderId, orderContext));
    }
}
