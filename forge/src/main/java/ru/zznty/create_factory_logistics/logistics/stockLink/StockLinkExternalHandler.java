package ru.zznty.create_factory_logistics.logistics.stockLink;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackageTarget;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

/**
 * Lets an external block (e.g. an AE2 ME Interface) stand in for a packager behind a
 * Create stock link: it both exposes available stock to the network ({@link #getStockSummary})
 * and fulfils requests ({@link #handle}).
 */
public interface StockLinkExternalHandler {
    BlockCapability<StockLinkExternalHandler, Direction> CAPABILITY =
            BlockCapability.create(
                    ResourceLocation.fromNamespaceAndPath(CreateFactoryLogistics.MODID, "stock_link_external"),
                    StockLinkExternalHandler.class, Direction.class);

    @Nullable
    Pair<GenericPackageTarget, GenericRequest> handle(PackagerLinkBlockEntity self, GenericStack stack, String address,
                                                      int linkIndex, MutableBoolean finalLink, int orderId,
                                                      @Nullable GenericOrder orderContext,
                                                      @Nullable IdentifiedInventory ignoredHandler);

    /**
     * The stock this handler makes available to the logistics network. Reported through the
     * stock link so the stock keeper counts it as a linked packager and lists its contents.
     *
     * @return the available stock; never null (return {@link GenericInventorySummary#empty()} if none)
     */
    GenericInventorySummary getStockSummary();
}
