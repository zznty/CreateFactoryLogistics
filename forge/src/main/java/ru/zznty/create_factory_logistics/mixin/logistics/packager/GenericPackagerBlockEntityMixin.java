package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
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
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.*;
import ru.zznty.create_factory_logistics.logistics.generic.GeneticInventoryBehaviour;
import ru.zznty.create_factory_logistics.logistics.packager.GenericPackagerItemHandler;

import java.util.*;
import java.util.function.Function;

import static com.simibubi.create.content.logistics.packager.PackagerBlockEntity.CYCLE;

@Mixin(PackagerBlockEntity.class)
public abstract class GenericPackagerBlockEntityMixin extends SmartBlockEntity implements GenericPackagerBlockEntity {
    @Shadow
    public InvManipulationBehaviour targetInventory;
    @Shadow
    public String signBasedAddress;
    @Shadow
    public List<BigItemStack> queuedExitingPackages;
    @Shadow
    public ItemStack heldBox, previouslyUnwrapped;
    @Shadow
    public int animationTicks, buttonCooldown;
    @Shadow
    public boolean animationInward;
    @Shadow
    private AdvancementBehaviour advancements;
    @Shadow
    private InventorySummary availableItems;

    @Shadow
    private VersionedInventoryTrackerBehaviour invVersionTracker;

    @Shadow
    public void triggerStockCheck() {
    }

    @Shadow
    private BlockPos getLinkPos() {
        return null;
    }

    @Shadow
    private boolean supportsBlockEntity(BlockEntity target) {
        return false;
    }

    @Unique
    private GeneticInventoryBehaviour createFactoryLogistics$inventoryBehaviour;

    public GenericPackagerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "addBehaviours",
            at = @At("TAIL"),
            remap = false
    )
    private void addBehaviours(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        behaviours.add(createFactoryLogistics$inventoryBehaviour = new GeneticInventoryBehaviour(this,
                                                                                                 CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing())
                .withFilter(this::supportsBlockEntity));
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

    @Overwrite
    public InventorySummary getAvailableItems() {
        if (availableItems != null && invVersionTracker.stillWaiting(targetInventory))
            return availableItems;

        GenericInventorySummary available = GenericInventorySummary.empty();

        if (createFactoryLogistics$inventoryBehaviour.hasInventory()) {
            @Nullable PackagerAttachedHandler handler = PackagerAttachedHandler.get(
                    (PackagerBlockEntity) (Object) this);
            if (handler != null) {
                GenericInventorySummaryProvider summaryProvider = createFactoryLogistics$inventoryBehaviour.getInventory().get(
                        handler.supportedKey());
                if (summaryProvider != null) {
                    summaryProvider.apply(available);
                    invVersionTracker.awaitNewVersion(targetInventory);
                    createFactoryLogistics$submitNewArrivals(
                            availableItems == null ? null : GenericInventorySummary.of(availableItems), available);
                }
            }
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

        PackagerAttachedHandler handler = PackagerAttachedHandler.get((PackagerBlockEntity) (Object) this);
        if (handler == null) return promiseQueues;

        for (Direction d : Iterate.directions) {
            if (!level.isLoaded(worldPosition.relative(d)))
                continue;

            BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
            if (adjacentState.is(handler.supportedGauge())) {
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

    @Overwrite
    public boolean unwrapBox(ItemStack box, boolean simulate) {
        if (animationTicks > 0)
            return false;

        Objects.requireNonNull(this.level);

        GenericOrder orderContext = GenericOrder.of(level.registryAccess(), box);
        Direction facing = getBlockState().getOptionalValue(PackagerBlock.FACING).orElse(Direction.UP);
        BlockPos target = worldPosition.relative(facing.getOpposite());
        BlockState targetState = level.getBlockState(target);

        ItemStack originalBox = box.copy();

        boolean unpacked = Optional.ofNullable(
                        PackagerAttachedHandler.get((PackagerBlockEntity) (Object) this)).map(handler ->
                                                                                                      handler.unwrap(
                                                                                                              level,
                                                                                                              target,
                                                                                                              targetState,
                                                                                                              facing,
                                                                                                              orderContext == null ?
                                                                                                              null :
                                                                                                              orderContext.asCrafting(),
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

    @Overwrite
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

        PackagerAttachedHandler target = PackagerAttachedHandler.get((PackagerBlockEntity) (Object) this);
        if (target == null)
            return Pair.of(ItemStack.EMPTY, null);

        boolean anyItemPresent = false;
        PackageBuilder extractedPackage = target.newPackage();
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

                for (int slot = 0; slot < target.slotCount(); slot++) {
                    int initialAmount = requestQueue ?
                                        Math.min(extractedPackage.maxPerSlot(), nextRequest.getCount()) :
                                        extractedPackage.maxPerSlot();
                    GenericStack extracted = target.extract(slot, initialAmount, true);
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
                    if (target.extract(slot, transferred, false).isEmpty())
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
            GenericOrder.set(level.registryAccess(), box, fixedOrderId, linkIndexInOrder, finalLinkInOrder,
                             packageIndexAtLink, finalPackageAtLink, orderContext);
        if (!requestQueue && !signBasedAddress.isBlank())
            PackageItem.addAddress(box, signBasedAddress);

        return Pair.of(box, extractedPackage);
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/nbt/NBTHelper;readCompoundList(Lnet/minecraft/nbt/ListTag;Ljava/util/function/Function;)Ljava/util/List;"
            )
    )
    private List<BigItemStack> readQueuedExitingPackages(ListTag listNBT,
                                                         Function<CompoundTag, BigItemStack> deserializer,
                                                         Operation<List<BigItemStack>> original) {
        return NBTHelper.readCompoundList(listNBT, t ->
                BigGenericStack.of(GenericStackSerializer.read(level.registryAccess(), t)).asStack());
    }

    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/nbt/NBTHelper;writeCompoundList(Ljava/lang/Iterable;Ljava/util/function/Function;)Lnet/minecraft/nbt/ListTag;"
            )
    )
    private ListTag writeQueuedExitingPackages(Iterable<BigItemStack> list,
                                               Function<BigItemStack, CompoundTag> serializer,
                                               Operation<ListTag> original) {
        return NBTHelper.writeCompoundList(list, t -> {
            CompoundTag tag = new CompoundTag();
            GenericStackSerializer.write(level.registryAccess(), BigGenericStack.of(t).get(), tag);
            return tag;
        });
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/codecs/CatnipCodecUtils;decodeOrNull(Lcom/mojang/serialization/Codec;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/lang/Object;"
            )
    )
    private Object readLastSummary(Codec<InventorySummary> codec, HolderLookup.Provider registries,
                                   Tag tag, Operation<Optional<InventorySummary>> original) {
        if (tag instanceof ListTag listTag) {
            GenericInventorySummary summary = GenericInventorySummary.empty();
            NBTHelper.iterateCompoundList(listTag, t -> summary.add(GenericStackSerializer.read(registries, t)));
            return summary;
        }
        return null;
    }

    @Redirect(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/codecs/CatnipCodecUtils;encode(Lcom/mojang/serialization/Codec;Lnet/minecraft/core/HolderLookup$Provider;Ljava/lang/Object;)Ljava/util/Optional;"
            )
    )
    private Optional<Tag> writeLastSummary(Codec<InventorySummary> codec, HolderLookup.Provider registries, Object t) {
        GenericInventorySummary summary = GenericInventorySummary.of((InventorySummary) t);
        return Optional.of(NBTHelper.writeCompoundList(summary.get(), stack -> {
            CompoundTag tag = new CompoundTag();
            GenericStackSerializer.write(registries, stack, tag);
            return tag;
        }));
    }

    @WrapMethod(
            method = "isSameInventoryFallback"
    )
    private static boolean isSameInventoryNullCheck(IItemHandler first, IItemHandler second,
                                                    Operation<Boolean> original) {
        if (first == null || second == null) return false;
        return original.call(first, second);
    }
}
