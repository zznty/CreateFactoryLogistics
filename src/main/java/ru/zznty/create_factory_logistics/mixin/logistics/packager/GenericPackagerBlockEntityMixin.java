package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packager.*;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.*;
import ru.zznty.create_factory_logistics.logistics.packager.GenericPackagerItemHandler;

import java.util.*;

import static com.simibubi.create.content.logistics.packager.PackagerBlockEntity.CYCLE;

@Mixin(PackagerBlockEntity.class)
public abstract class GenericPackagerBlockEntityMixin extends SmartBlockEntity implements GenericPackagerBlockEntity {
    @Shadow(remap = false)
    public InvManipulationBehaviour targetInventory;
    @Shadow(remap = false)
    public String signBasedAddress;
    @Shadow(remap = false)
    public List<BigItemStack> queuedExitingPackages;
    @Shadow(remap = false)
    public ItemStack heldBox, previouslyUnwrapped;
    @Shadow(remap = false)
    public int animationTicks, buttonCooldown;
    @Shadow(remap = false)
    public boolean animationInward;
    @Shadow(remap = false)
    private AdvancementBehaviour advancements;
    @Shadow(remap = false)
    private InventorySummary availableItems;

    @Shadow(remap = false)
    public void triggerStockCheck() {
    }

    @Shadow(remap = false)
    private BlockPos getLinkPos() {
        return null;
    }

    @Shadow
    public abstract <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side);

    public GenericPackagerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lcom/simibubi/create/content/logistics/packager/PackagerBlockEntity;)Lcom/simibubi/create/content/logistics/packager/PackagerItemHandler;"
            )
    )
    private PackagerItemHandler createInventory(PackagerBlockEntity blockEntity) {
        return new GenericPackagerItemHandler(blockEntity);
    }

    @Overwrite(remap = false)
    public InventorySummary getAvailableItems(boolean scanInputSlots) {
        Optional<PackagerAttachedHandler> handler = getCapability(AbstractionsCapabilities.PACKAGER_ATTACHED).resolve();

        if (availableItems != null && handler.isPresent() && !handler.get().hasChanges())
            return availableItems;

        GenericInventorySummary available = GenericInventorySummary.empty();

        if (handler.isPresent()) {
            handler.get().collectAvailable(scanInputSlots, available);
            createFactoryLogistics$submitNewArrivals(
                    availableItems == null ? null : GenericInventorySummary.of(availableItems), available);
        }

        availableItems = available.asSummary();
        return availableItems;
    }

    @Unique
    private void createFactoryLogistics$submitNewArrivals(GenericInventorySummary before,
                                                          GenericInventorySummary after) {
        if (before == null || after.isEmpty())
            return;

        Set<GenericPromiseQueue> promiseQueues = createFactoryLogistics$collectAdjacentQueues();

        if (promiseQueues.isEmpty())
            return;

        for (GenericStack stack : after.get()) {
            before.add(stack.withAmount(-stack.amount()));
        }

        for (GenericPromiseQueue queue : promiseQueues) {
            for (GenericStack stack : before.get()) {
                if (stack.amount() < 0)
                    queue.stackEnteredSystem(stack.withAmount(-stack.amount()));
            }
        }
    }

    @Unique
    private Set<GenericPromiseQueue> createFactoryLogistics$collectAdjacentQueues() {
        Set<GenericPromiseQueue> promiseQueues = new HashSet<>();

        Objects.requireNonNull(level);

        Optional<PackagerAttachedHandler> handler = getCapability(AbstractionsCapabilities.PACKAGER_ATTACHED).resolve();
        if (handler.isEmpty()) return promiseQueues;

        for (Direction d : Iterate.directions) {
            if (!level.isLoaded(worldPosition.relative(d)))
                continue;

            BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
            if (adjacentState.is(handler.get().supportedGauge())) {
                if (FactoryPanelBlock.connectedDirection(adjacentState) != d)
                    continue;
                if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof FactoryPanelBlockEntity fpbe))
                    continue;
                if (!fpbe.restocker)
                    continue;
                for (FactoryPanelBehaviour behaviour : fpbe.panels.values()) {
                    if (!behaviour.isActive())
                        continue;
                    promiseQueues.add((GenericPromiseQueue) behaviour.restockerPromises);
                }
            }

            if (AllBlocks.STOCK_LINK.has(adjacentState)) {
                if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                    continue;
                if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof PackagerLinkBlockEntity plbe))
                    continue;
                UUID freqId = plbe.behaviour.freqId;
                if (!Create.LOGISTICS.hasQueuedPromises(freqId))
                    continue;
                promiseQueues.add((GenericPromiseQueue) Create.LOGISTICS.getQueuedPromises(freqId));
            }
        }

        return promiseQueues;
    }

    @Overwrite(remap = false)
    public boolean unwrapBox(ItemStack box, boolean simulate) {
        if (animationTicks > 0)
            return false;

        Objects.requireNonNull(this.level);

        PackageOrderWithCrafts orderContext = PackageItem.getOrderContext(box);
        Direction facing = getBlockState().getOptionalValue(PackagerBlock.FACING).orElse(Direction.UP);
        BlockPos target = worldPosition.relative(facing.getOpposite());
        BlockState targetState = level.getBlockState(target);

        ItemStack originalBox = box.copy();

        boolean unpacked = getCapability(AbstractionsCapabilities.PACKAGER_ATTACHED).map(handler ->
                                                                                                 handler.unwrap(level,
                                                                                                                target,
                                                                                                                targetState,
                                                                                                                facing,
                                                                                                                orderContext,
                                                                                                                box,
                                                                                                                simulate))
                .orElse(false);

        if (unpacked && !simulate) {
            previouslyUnwrapped = originalBox;
            animationInward = true;
            animationTicks = CYCLE;
            notifyUpdate();
        }

        return unpacked;
    }

    @Overwrite(remap = false)
    public void attemptToSend(List<PackagingRequest> queuedRequests) {
        if (queuedRequests == null && (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0))
            return;

        if (queuedRequests == null) {
            attemptToSendGeneric(null);
            return;
        }

        // use attemptToSendIngredients instead
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void attemptToSendGeneric(Collection<GenericRequest> queuedRequests) {
        Pair<ItemStack, PackageBuilder> extracted = createFactoryLogistics$extractBox(queuedRequests);

        if (extracted.getFirst().isEmpty())
            return;

        BlockPos linkPos = getLinkPos();
        if (linkPos != null
                && level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity plbe)
            createFactoryLogistics$deductFromAccurateSummary(plbe.behaviour, extracted.getSecond());

        if (!heldBox.isEmpty() || animationTicks != 0) {
            queuedExitingPackages.add(new BigItemStack(extracted.getFirst()));
            return;
        }

        heldBox = extracted.getFirst();
        animationInward = false;
        animationTicks = CYCLE;

        advancements.awardPlayer(AllAdvancements.PACKAGER);
        triggerStockCheck();
        notifyUpdate();
    }

    @Unique
    public void createFactoryLogistics$deductFromAccurateSummary(LogisticallyLinkedBehaviour behaviour,
                                                                 PackageBuilder content) {
        InventorySummary accurateSummary = LogisticsManager.ACCURATE_SUMMARIES.getIfPresent(
                behaviour.freqId);
        if (accurateSummary == null) return;
        GenericInventorySummary summary = GenericInventorySummary.of(accurateSummary);

        for (GenericStack stack : content.content()) {
            summary.add(stack.withAmount(-Math.min(summary.getCountOf(stack.key()), stack.amount())));
        }
    }

    @Unique
    public Pair<ItemStack, PackageBuilder> createFactoryLogistics$extractBox(
            Collection<GenericRequest> queuedRequests) {
        boolean requestQueue = queuedRequests != null;

        // Data written to packages for defrags
        int linkIndexInOrder = 0;
        boolean finalLinkInOrder = false;
        int packageIndexAtLink = 0;
        boolean finalPackageAtLink = false;
        GenericOrder orderContext = null;
        int fixedOrderId = 0;
        String fixedAddress = null;

        Optional<PackagerAttachedHandler> target = getCapability(AbstractionsCapabilities.PACKAGER_ATTACHED).resolve();
        if (target.isEmpty())
            return Pair.of(ItemStack.EMPTY, null);

        boolean anyItemPresent = false;
        PackageBuilder extractedPackage = target.get().newPackage();
        GenericRequest nextRequest = null;
        Iterator<GenericRequest> requestIterator = null;

        if (requestQueue && !queuedRequests.isEmpty()) {
            requestIterator = queuedRequests.iterator();
            nextRequest = requestIterator.next();
            fixedAddress = nextRequest.address();
            fixedOrderId = nextRequest.orderId();
            linkIndexInOrder = nextRequest.linkIndex();
            finalLinkInOrder = nextRequest.finalLink()
                    .booleanValue();
            packageIndexAtLink = nextRequest.packageCounter()
                    .getAndIncrement();
            orderContext = nextRequest.context();
        }

        Outer:
        for (int i = 0; i < extractedPackage.slotCount(); i++) {
            boolean continuePacking = true;

            while (continuePacking) {
                continuePacking = false;

                for (int slot = 0; slot < target.get().slotCount(); slot++) {
                    int initialAmount = requestQueue ?
                                        Math.min(extractedPackage.maxPerSlot(), nextRequest.getCount()) :
                                        extractedPackage.maxPerSlot();
                    GenericStack extracted = target.get().extract(slot, initialAmount, true);
                    if (extracted.isEmpty())
                        continue;
                    if (requestQueue && !nextRequest.stack().canStack(extracted))
                        continue;

                    boolean bulky = extractedPackage.measure(extracted.key()) == PackageMeasureResult.BULKY;
                    if (bulky && anyItemPresent)
                        continue;

                    int leftovers = extractedPackage.add(extracted);

                    // content is not stackable with currently contained ingredients
                    if (leftovers < 0) continue;

                    int transferred = extracted.amount() - leftovers;
                    if (target.get().extract(slot, transferred, false).isEmpty())
                        continue;
                    anyItemPresent = true;

                    if (!requestQueue) {
                        if (bulky)
                            break Outer;
                        continue;
                    }

                    nextRequest.subtract(transferred);

                    if (!nextRequest.isEmpty()) {
                        if (bulky)
                            break Outer;
                        continue;
                    }

                    finalPackageAtLink = true;
                    requestIterator.remove();
                    if (!requestIterator.hasNext())
                        break Outer;
                    int previousCount = nextRequest.packageCounter()
                            .intValue();
                    nextRequest = requestIterator.next();
                    if (!fixedAddress.equals(nextRequest.address()))
                        break Outer;
                    if (fixedOrderId != nextRequest.orderId())
                        break Outer;

                    nextRequest.packageCounter()
                            .setValue(previousCount);
                    finalPackageAtLink = false;
                    continuePacking = true;
                    if (nextRequest.context() != null)
                        orderContext = nextRequest.context();

                    if (bulky)
                        break Outer;
                    break;
                }
            }
        }

//        if (!anyItemPresent && nextRequest != null) requestIterator.remove();

        ItemStack box = extractedPackage.build();

        if (box.isEmpty())
            return Pair.of(ItemStack.EMPTY, null);

        PackageItem.clearAddress(box);

        if (fixedAddress != null)
            PackageItem.addAddress(box, fixedAddress);
        if (requestQueue)
            GenericOrder.set(box, fixedOrderId, linkIndexInOrder, finalLinkInOrder, packageIndexAtLink,
                             finalPackageAtLink, orderContext);
        if (!requestQueue && !signBasedAddress.isBlank())
            PackageItem.addAddress(box, signBasedAddress);

        return Pair.of(box, extractedPackage);
    }
}
