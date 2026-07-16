package ru.zznty.create_factory_logistics.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.helpers.InterfaceLogic;
import appeng.parts.misc.InterfacePart;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackageTarget;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.stockLink.StockLinkExternalHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class AE2Integration {
    private static final List<WeakReference<InterfaceBlockEntity>> pendingUnpack = new ArrayList<>();
    // Requests waiting to be thrown out of a Frogport, one package per animation cycle.
    // Extraction from AE2 is deferred until each package is actually thrown, so nothing is
    // lost if throws are still pending.
    private static final List<PendingExport> pendingExports = new ArrayList<>();
    private static int tickCounter = 0;

    private record PendingExport(BaseStockLinkHandler handler, GenericRequest request) {
    }

    public static void register(IEventBus bus) {
        registerKeyAdapters();
        bus.addListener(AE2Integration::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(AE2Integration::onServerTick);
    }

    private static void registerKeyAdapters() {
        ResourceLocation itemKey = ResourceLocation.fromNamespaceAndPath(GenericContentExtender.ID, "item");
        AEGenericKeyAdapters.register(itemKey, new AEGenericKeyAdapter() {
            @Override
            @Nullable
            public AEKey toAEKey(GenericKey key, int amount) {
                return key instanceof ItemKey ik ? AEItemKey.of(ik.stack()) : null;
            }

            @Override
            @Nullable
            public GenericStack toGenericStack(AEKey key, long amount) {
                return key instanceof AEItemKey ik
                        ? GenericStack.wrap(ik.toStack()).withAmount((int) amount)
                        : null;
            }
        });

        ResourceLocation fluidKey = ResourceLocation.fromNamespaceAndPath(GenericContentExtender.ID, "fluid");
        AEGenericKeyAdapters.register(fluidKey, new AEGenericKeyAdapter() {
            @Override
            @Nullable
            public AEKey toAEKey(GenericKey key, int amount) {
                return key instanceof FluidKey fk ? AEFluidKey.of(fk.stack()) : null;
            }

            @Override
            @Nullable
            public GenericStack toGenericStack(AEKey key, long amount) {
                return key instanceof AEFluidKey fk
                        ? new GenericStack(new FluidKey(fk.getFluid().builtInRegistryHolder(), null), (int) amount)
                        : null;
            }
        });
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                StockLinkExternalHandler.CAPABILITY,
                AEBlockEntities.INTERFACE.get(),
                (iface, side) -> new InterfaceStockLinkHandler(iface));

        event.registerBlockEntity(
                StockLinkExternalHandler.CAPABILITY,
                AEBlockEntities.CABLE_BUS.get(),
                (cableBus, side) -> {
                    if (side != null && cableBus.getPart(side) instanceof InterfacePart ipart)
                        return new CableBusStockLinkHandler(cableBus, ipart);
                    return null;
                });
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        // Feed pending outgoing packages into their Frogport, one per animation cycle
        if (!pendingExports.isEmpty()) {
            Iterator<PendingExport> exportIt = pendingExports.iterator();
            while (exportIt.hasNext()) {
                PendingExport pending = exportIt.next();
                if (!pending.handler().pumpExport(pending.request()))
                    exportIt.remove();
            }
        }

        if (tickCounter % 20 != 0 || pendingUnpack.isEmpty())
            return;

        Iterator<WeakReference<InterfaceBlockEntity>> it = pendingUnpack.iterator();
        while (it.hasNext()) {
            InterfaceBlockEntity iface = it.next().get();
            if (iface == null || iface.isRemoved()) {
                it.remove();
                continue;
            }
            boolean hadPackages = unpackPendingPackages(iface);
            if (!hadPackages)
                it.remove();
        }
    }

    public static void enqueueUnpack(InterfaceBlockEntity iface) {
        for (WeakReference<InterfaceBlockEntity> ref : pendingUnpack) {
            if (ref.get() == iface)
                return;
        }
        pendingUnpack.add(new WeakReference<>(iface));
    }

    private static boolean unpackPendingPackages(InterfaceBlockEntity iface) {
        InterfaceLogic logic = iface.getInterfaceLogic();
        appeng.util.ConfigInventory storage = logic.getStorage();
        boolean found = false;

        for (int i = 0; i < storage.size(); i++) {
            appeng.api.stacks.GenericStack slotStack = storage.getStack(i);
            if (slotStack == null)
                continue;
            AEKey key = slotStack.what();
            if (!(key instanceof AEItemKey itemKey))
                continue;

            ItemStack itemStack = itemKey.toStack();
            if (!PackageItem.isPackage(itemStack))
                continue;

            found = true;
            unpackPackageIntoAE2(logic, iface.getLevel(), itemStack);
            storage.extract(i, key, 1, appeng.api.config.Actionable.MODULATE);
        }
        return found;
    }

    private static void unpackPackageIntoAE2(InterfaceLogic logic, Level level, ItemStack pkg) {
        if (!(level instanceof ServerLevel))
            return;

        IGridNode gridNode = logic.getActionableNode();
        if (gridNode == null || gridNode.getGrid() == null)
            return;

        IGrid grid = gridNode.getGrid();
        IEnergyService energy = grid.getEnergyService();
        MEStorage meStorage = grid.getStorageService().getInventory();
        IActionSource source = IActionSource.ofMachine(logic);

        GenericInventory inventory = GenericInventory.of(pkg);
        GenericInventorySummary summary = GenericInventorySummary.empty();
        for (GenericKeyRegistration reg : GenericContentExtender.REGISTRATIONS.values()) {
            GenericInventorySummaryProvider provider = inventory.get(reg);
            if (provider != null)
                provider.apply(summary, level.registryAccess());
        }

        for (GenericStack stack : summary.get()) {
            if (stack.amount() <= 0)
                continue;
            AEKey aeKey = AEGenericKeyAdapters.toAEKey(stack);
            if (aeKey != null)
                StorageHelper.poweredInsert(energy, meStorage, aeKey, stack.amount(), source);
        }
    }

    /**
     * Bridges a Create stock link to an AE2 interface. Behaves like a packager: requested
     * stacks are extracted from the ME network, packed into one or more packages/jars, and
     * placed into the package port (frogport) sitting directly above the interface.
     */
    private abstract static class BaseStockLinkHandler implements StockLinkExternalHandler, GenericPackageTarget {
        protected abstract InterfaceLogic getLogic();

        protected abstract BlockEntity getBlockEntity();

        @Override
        @Nullable
        public Pair<GenericPackageTarget, GenericRequest> handle(
                PackagerLinkBlockEntity self, GenericStack stack, String address,
                int linkIndex, MutableBoolean finalLink, int orderId,
                @Nullable GenericOrder orderContext,
                @Nullable IdentifiedInventory ignoredHandler) {

            // Can only dispatch through a package port placed above the interface
            if (getPackagePort() == null)
                return null;
            // Must be able to bridge this key type to an AE2 key
            AEKey aeKey = AEGenericKeyAdapters.toAEKey(stack);
            if (aeKey == null)
                return null;

            MeContext ctx = meContext();
            if (ctx == null)
                return null;

            long available = StorageHelper.poweredExtraction(ctx.energy(), ctx.storage(), aeKey, stack.amount(),
                    ctx.source(), Actionable.SIMULATE);
            if (available <= 0)
                return null;

            int provide = (int) Math.min(stack.amount(), available);
            return Pair.of(this, GenericRequest.create(stack.withAmount(provide), provide, address,
                    linkIndex, finalLink, 0, orderId, orderContext));
        }

        @Override
        public boolean isTooBusyFor(LogisticallyLinkedBehaviour.RequestType type) {
            // Packages are pushed straight into the port during dispatch, so backpressure
            // is naturally bounded by the port's free space; never pre-emptively too busy.
            return false;
        }

        @Override
        public GenericInventorySummary getStockSummary() {
            GenericInventorySummary summary = GenericInventorySummary.empty();
            MeContext ctx = meContext();
            if (ctx == null)
                return summary;

            for (Object2LongMap.Entry<AEKey> entry : ctx.storage().getAvailableStacks()) {
                long amount = entry.getLongValue();
                if (amount <= 0)
                    continue;
                GenericStack stack = AEGenericKeyAdapters.toGenericStack(entry.getKey(),
                        Math.min(amount, Integer.MAX_VALUE));
                if (stack != null && !stack.isEmpty())
                    summary.add(stack);
            }
            return summary;
        }

        @Override
        public void dispatch(Collection<GenericRequest> queuedRequests) {
            if (queuedRequests.isEmpty())
                return;

            PackagePortBlockEntity port = getPackagePort();
            BlockEntity be = getBlockEntity();
            if (port == null || be == null || !(be.getLevel() instanceof ServerLevel level)) {
                queuedRequests.clear();
                return;
            }
            // Without an export target the port has nowhere to send packages.
            if (port.target == null) {
                queuedRequests.clear();
                return;
            }

            flashLink(level, be.getBlockPos());

            // Defer the actual extraction+throw to the server tick, so packages are pushed into
            // the Frogport and animated out one at a time (and AE2 is only drained per throw).
            for (Iterator<GenericRequest> it = queuedRequests.iterator(); it.hasNext(); ) {
                GenericRequest request = it.next();
                if (!request.isEmpty())
                    pendingExports.add(new PendingExport(this, request));
                it.remove();
            }
        }

        /**
         * Pings the connected stock link with the wireless effect, mirroring
         * {@code PackagerBlockEntity#flashLink} so requests routed through the AE2
         * interface visually flash the link just like a real packager.
         */
        private void flashLink(Level level, BlockPos interfacePos) {
            for (Direction d : Direction.values()) {
                BlockState adjacent = level.getBlockState(interfacePos.relative(d));
                if (!AllBlocks.STOCK_LINK.has(adjacent))
                    continue;
                if (PackagerLinkBlock.getConnectedDirection(adjacent) != d)
                    continue;
                WiFiEffectPacket.send(level, interfacePos.relative(d));
                return;
            }
        }

        /**
         * Throws the next package for a pending request out of the Frogport (with animation),
         * extracting exactly that package's worth from AE2 only now. One package per idle cycle.
         *
         * @return true while the request still has packages left to throw (call again later),
         * false when finished or no longer serviceable
         */
        private boolean pumpExport(GenericRequest request) {
            if (request.isEmpty())
                return false;

            BlockEntity be = getBlockEntity();
            if (be == null || be.isRemoved() || !(be.getLevel() instanceof ServerLevel level))
                return false;

            PackagePortBlockEntity port = getPackagePort();
            if (port == null || port.target == null)
                return false;

            // Wait for the Frogport to finish its current throw before feeding the next package
            if (port instanceof FrogportBlockEntity frog && frog.isAnimationInProgress())
                return true;

            MeContext ctx = meContext();
            if (ctx == null)
                return false;

            ItemStack box = buildOnePackage(level, ctx, request);
            if (box.isEmpty())
                return false; // AE2 ran dry or the package could not be built

            if (port instanceof FrogportBlockEntity frog) {
                // Hands the package to the target on animation completion (drops it if refused)
                frog.startAnimation(box, true);
            } else if (!port.target.export(level, port.getBlockPos(), box, false)) {
                // Non-animated port refused delivery: return the package contents to AE2
                for (GenericStack content : contentsOf(level, box))
                    reinsert(ctx, content);
                return false;
            }
            port.notifyUpdate();

            return !request.isEmpty();
        }

        /**
         * Extracts one package worth of the request from AE2 and builds the package (address +
         * order data set). Advances the request's counters. Returns EMPTY if nothing could be built.
         */
        private ItemStack buildOnePackage(ServerLevel level, MeContext ctx, GenericRequest request) {
            GenericStack stack = request.stack();

            GenericKeyRegistration reg = GenericContentExtender.registrationOf(stack.key());
            if (reg == null)
                return ItemStack.EMPTY;
            Supplier<PackageBuilder> builderSupplier = reg.<GenericKey>provider().packageBuilder();
            if (builderSupplier == null)
                return ItemStack.EMPTY;
            AEKey aeKey = AEGenericKeyAdapters.toAEKey(stack);
            if (aeKey == null)
                return ItemStack.EMPTY;

            PackageBuilder builder = builderSupplier.get();
            int packed = 0;
            while (!builder.isFull() && packed < request.getCount()) {
                int chunk = Math.min(builder.maxPerSlot(), request.getCount() - packed);
                if (chunk <= 0)
                    break;
                long avail = StorageHelper.poweredExtraction(ctx.energy(), ctx.storage(), aeKey, chunk,
                        ctx.source(), Actionable.SIMULATE);
                if (avail <= 0)
                    break;
                chunk = (int) Math.min(chunk, avail);
                int leftover = builder.add(stack.withAmount(chunk));
                if (leftover < 0)
                    break; // cannot stack with existing contents
                int added = chunk - Math.max(leftover, 0);
                if (added <= 0)
                    break; // package is full
                packed += added;
            }
            if (packed <= 0)
                return ItemStack.EMPTY;

            long extracted = StorageHelper.poweredExtraction(ctx.energy(), ctx.storage(), aeKey, packed,
                    ctx.source(), Actionable.MODULATE);
            if (extracted <= 0)
                return ItemStack.EMPTY;

            if (extracted < packed) {
                // Rare shortfall between simulate and modulate: rebuild for what we actually got
                builder = builderSupplier.get();
                builder.add(stack.withAmount((int) extracted));
            }
            ItemStack box = builder.build();
            if (box.isEmpty()) {
                StorageHelper.poweredInsert(ctx.energy(), ctx.storage(), aeKey, extracted, ctx.source());
                return ItemStack.EMPTY;
            }

            int packageIndex = request.packageCounter().getAndIncrement();
            request.subtract((int) extracted);
            boolean finalPackage = request.isEmpty();

            PackageItem.clearAddress(box);
            if (request.address() != null && !request.address().isEmpty())
                PackageItem.addAddress(box, request.address());
            GenericOrder.set(level.registryAccess(), box, request.orderId(), request.linkIndex(),
                    request.finalLink().booleanValue(), packageIndex, finalPackage, request.context());
            return box;
        }

        private List<GenericStack> contentsOf(Level level, ItemStack box) {
            GenericInventory inventory = GenericInventory.of(box);
            GenericInventorySummary summary = GenericInventorySummary.empty();
            for (GenericKeyRegistration reg : GenericContentExtender.REGISTRATIONS.values()) {
                GenericInventorySummaryProvider provider = inventory.get(reg);
                if (provider != null)
                    provider.apply(summary, level.registryAccess());
            }
            return summary.get();
        }

        private void reinsert(MeContext ctx, GenericStack content) {
            if (content.amount() <= 0)
                return;
            AEKey aeKey = AEGenericKeyAdapters.toAEKey(content);
            if (aeKey != null)
                StorageHelper.poweredInsert(ctx.energy(), ctx.storage(), aeKey, content.amount(), ctx.source());
        }

        @Nullable
        private PackagePortBlockEntity getPackagePort() {
            BlockEntity be = getBlockEntity();
            if (be == null || be.getLevel() == null)
                return null;
            return be.getLevel().getBlockEntity(be.getBlockPos().above()) instanceof PackagePortBlockEntity port
                    ? port : null;
        }

        @Nullable
        private MeContext meContext() {
            InterfaceLogic logic = getLogic();
            IGridNode node = logic.getActionableNode();
            if (node == null || node.getGrid() == null)
                return null;
            IGrid grid = node.getGrid();
            return new MeContext(grid.getEnergyService(), grid.getStorageService().getInventory(),
                    IActionSource.ofMachine(logic));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof BaseStockLinkHandler other))
                return false;
            return Objects.equals(getBlockEntity(), other.getBlockEntity());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getBlockEntity());
        }
    }

    private record MeContext(IEnergyService energy, MEStorage storage, IActionSource source) {
    }

    private static class InterfaceStockLinkHandler extends BaseStockLinkHandler {
        private final InterfaceBlockEntity iface;

        InterfaceStockLinkHandler(InterfaceBlockEntity iface) {
            this.iface = iface;
        }

        @Override
        protected InterfaceLogic getLogic() {
            enqueueUnpack(iface);
            return iface.getInterfaceLogic();
        }

        @Override
        protected BlockEntity getBlockEntity() {
            return iface;
        }
    }

    private static class CableBusStockLinkHandler extends BaseStockLinkHandler {
        private final CableBusBlockEntity cableBus;
        private final InterfacePart ipart;

        CableBusStockLinkHandler(CableBusBlockEntity cableBus, InterfacePart ipart) {
            this.cableBus = cableBus;
            this.ipart = ipart;
        }

        @Override
        protected InterfaceLogic getLogic() {
            return ipart.getInterfaceLogic();
        }

        @Override
        protected BlockEntity getBlockEntity() {
            return cableBus;
        }
    }
}
