package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
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
import ru.zznty.create_factory_logistics.FactoryCapabilities;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageBuilder;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageMeasureResult;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientPromiseQueue;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRequest;
import ru.zznty.create_factory_logistics.logistics.panel.request.PackagerIngredientBlockEntity;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.*;

import static com.simibubi.create.content.logistics.packager.PackagerBlockEntity.CYCLE;

@Mixin(PackagerBlockEntity.class)
public abstract class PackagerIngredientBlockEntityMixin extends SmartBlockEntity implements PackagerIngredientBlockEntity {
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

    public PackagerIngredientBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Overwrite(remap = false)
    public InventorySummary getAvailableItems(boolean scanInputSlots) {
        Optional<PackagerAttachedHandler> handler = getCapability(FactoryCapabilities.PACKAGER_ATTACHED).resolve();

        if (availableItems != null && handler.isPresent() && !handler.get().hasChanges())
            return availableItems;

        InventorySummary available = new InventorySummary();

        if (handler.isPresent()) {
            handler.get().collectAvailable(scanInputSlots, (IngredientInventorySummary) available);
            createFactoryLogistics$submitNewArrivals((IngredientInventorySummary) availableItems, (IngredientInventorySummary) available);
        }

        availableItems = available;
        return availableItems;
    }

    @Unique
    private void createFactoryLogistics$submitNewArrivals(IngredientInventorySummary before, IngredientInventorySummary after) {
        if (before == null || after.isEmpty())
            return;

        Set<IngredientPromiseQueue> promiseQueues = createFactoryLogistics$collectAdjacentQueues();

        if (promiseQueues.isEmpty())
            return;

        for (BoardIngredient ingredient : after.get()) {
            before.add(ingredient.withAmount(-ingredient.amount()));
        }

        for (IngredientPromiseQueue queue : promiseQueues) {
            for (BoardIngredient ingredient : before.get()) {
                if (ingredient.amount() < 0)
                    queue.ingredientEnteredSystem(ingredient.withAmount(-ingredient.amount()));
            }
        }
    }

    @Unique
    private Set<IngredientPromiseQueue> createFactoryLogistics$collectAdjacentQueues() {
        Set<IngredientPromiseQueue> promiseQueues = new HashSet<>();

        Objects.requireNonNull(level);

        Optional<PackagerAttachedHandler> handler = getCapability(FactoryCapabilities.PACKAGER_ATTACHED).resolve();
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
                    promiseQueues.add((IngredientPromiseQueue) behaviour.restockerPromises);
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
                promiseQueues.add((IngredientPromiseQueue) Create.LOGISTICS.getQueuedPromises(freqId));
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

        boolean unpacked = getCapability(FactoryCapabilities.PACKAGER_ATTACHED).map(handler ->
                        handler.unwrap(level, target, targetState, facing, orderContext, box, simulate))
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
            attemptToSendIngredients(null);
            return;
        }

        // use attemptToSendIngredients instead
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void attemptToSendIngredients(List<IngredientRequest> queuedRequests) {
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
    public void createFactoryLogistics$deductFromAccurateSummary(LogisticallyLinkedBehaviour behaviour, PackageBuilder content) {
        IngredientInventorySummary summary = (IngredientInventorySummary) LogisticsManager.ACCURATE_SUMMARIES.getIfPresent(behaviour.freqId);
        if (summary == null)
            return;

        for (BoardIngredient ingredient : content.content()) {
            summary.add(ingredient.withAmount(-Math.min(summary.getCountOf(ingredient.key()), ingredient.amount())));
        }
    }

    @Unique
    public Pair<ItemStack, PackageBuilder> createFactoryLogistics$extractBox(List<IngredientRequest> queuedRequests) {
        boolean requestQueue = queuedRequests != null;

        // Data written to packages for defrags
        int linkIndexInOrder = 0;
        boolean finalLinkInOrder = false;
        int packageIndexAtLink = 0;
        boolean finalPackageAtLink = false;
        IngredientOrder orderContext = null;
        int fixedOrderId = 0;
        String fixedAddress = null;

        Optional<PackagerAttachedHandler> target = getCapability(FactoryCapabilities.PACKAGER_ATTACHED).resolve();
        if (target.isEmpty())
            return Pair.of(ItemStack.EMPTY, null);

        boolean anyItemPresent = false;
        PackageBuilder extractedPackage = target.get().newPackage();
        IngredientRequest nextRequest = null;

        if (requestQueue && !queuedRequests.isEmpty()) {
            nextRequest = queuedRequests.get(0);
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
                    int initialAmount = requestQueue ? Math.min(extractedPackage.maxPerSlot(), nextRequest.getCount()) : extractedPackage.maxPerSlot();
                    BoardIngredient extracted = target.get().extract(slot, initialAmount, true);
                    if (extracted.isEmpty())
                        continue;
                    if (requestQueue && !nextRequest.ingredient().canStack(extracted))
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
                    queuedRequests.remove(0);
                    if (queuedRequests.isEmpty())
                        break Outer;
                    int previousCount = nextRequest.packageCounter()
                            .intValue();
                    nextRequest = queuedRequests.get(0);
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

        if (!anyItemPresent && nextRequest != null) queuedRequests.remove(0);

        ItemStack box = extractedPackage.build();

        if (box.isEmpty())
            return Pair.of(ItemStack.EMPTY, null);

        PackageItem.clearAddress(box);

        if (fixedAddress != null)
            PackageItem.addAddress(box, fixedAddress);
        if (requestQueue)
            IngredientOrder.set(box, fixedOrderId, linkIndexInOrder, finalLinkInOrder, packageIndexAtLink, finalPackageAtLink, orderContext);
        if (!requestQueue && !signBasedAddress.isBlank())
            PackageItem.addAddress(box, signBasedAddress);

        return Pair.of(box, extractedPackage);
    }

    @Override
    public BlockPos getLink() {
        return getLinkPos();
    }
}
