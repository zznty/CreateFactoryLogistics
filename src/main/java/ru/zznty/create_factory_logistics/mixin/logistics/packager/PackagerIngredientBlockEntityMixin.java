package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlockEntity;
import ru.zznty.create_factory_logistics.logistics.panel.request.*;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

import java.util.List;
import java.util.Optional;

@Mixin(PackagerBlockEntity.class)
public abstract class PackagerIngredientBlockEntityMixin extends SmartBlockEntity implements PackagerIngredientBlockEntity {
    @Shadow(remap = false)
    public InvManipulationBehaviour targetInventory;
    @Shadow(remap = false)
    public String signBasedAddress;
    @Shadow(remap = false)
    public List<BigItemStack> queuedExitingPackages;
    @Shadow(remap = false)
    public ItemStack heldBox;
    @Shadow(remap = false)
    public int animationTicks;
    @Shadow(remap = false)
    public boolean animationInward;
    @Shadow(remap = false)
    private AdvancementBehaviour advancements;

    @Shadow(remap = false)
    public void triggerStockCheck() {
    }

    @Shadow(remap = false)
    private BlockPos getLinkPos() {
        return null;
    }

    public PackagerIngredientBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void attemptToSendIngredients(List<IngredientRequest> queuedRequests) {
        ItemStack createdBox = createFactoryLogistics$extractBox(queuedRequests);

        if (createdBox.isEmpty())
            return;

        BlockPos linkPos = getLinkPos();
        if (linkPos != null
                && level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity plbe)
            createFactoryLogistics$deductFromAccurateSummary(plbe.behaviour, createdBox);

        if (!heldBox.isEmpty() || animationTicks != 0) {
            queuedExitingPackages.add(new BigItemStack(createdBox));
            return;
        }

        heldBox = createdBox;
        animationInward = false;
        animationTicks = PackagerBlockEntity.CYCLE;

        advancements.awardPlayer(AllAdvancements.PACKAGER);
        triggerStockCheck();
        notifyUpdate();
    }

    @Unique
    public void createFactoryLogistics$deductFromAccurateSummary(LogisticallyLinkedBehaviour behaviour, ItemStack box) {
        InventorySummary summary = LogisticsManager.ACCURATE_SUMMARIES.getIfPresent(behaviour.freqId);
        if (summary == null)
            return;
        if (box.getItem() instanceof JarPackageItem) {
            IFluidInventorySummary fluidInventorySummary = (IFluidInventorySummary) summary;

            FluidStack contents = GenericItemEmptying.emptyItem(level, box, true).getFirst();
            if (contents == FluidStack.EMPTY) return;
            contents = contents.copy();
            contents.setAmount(-Math.min(fluidInventorySummary.getCountOf(contents.getFluid()), contents.getAmount()));

            fluidInventorySummary.add(contents);
        } else {
            ItemStackHandler packageContents = PackageItem.getContents(box);
            for (int i = 0; i < packageContents.getSlots(); i++) {
                ItemStack orderedStack = packageContents.getStackInSlot(i);
                if (orderedStack.isEmpty())
                    continue;
                summary.add(orderedStack, -Math.min(summary.getCountOf(orderedStack), orderedStack.getCount()));
            }
        }
    }

    @Unique
    public ItemStack createFactoryLogistics$extractBox(List<IngredientRequest> queuedRequests) {
        // promised to be non-empty
        ItemStack box;

        boolean requestQueue = queuedRequests != null;

        // Data written to packages for defrags
        int linkIndexInOrder = 0;
        boolean finalLinkInOrder = false;
        int packageIndexAtLink = 0;
        boolean finalPackageAtLink = false;
        IngredientOrder orderContext = null;
        int fixedOrderId = 0;
        String fixedAddress = null;

        if ((Object) this instanceof JarPackagerBlockEntity jarPackagerBE) {
            if (requestQueue) {

                IngredientRequest request = queuedRequests.remove(0);
                box = jarPackagerBE.extractJar(request.ingredient().withAmount(request.getCount()));

                Optional<FluidStack> fluidContained = FluidUtil.getFluidContained(box);

                if (fluidContained.isPresent() && fluidContained.get().getAmount() < request.getCount()) {
                    request.subtract(fluidContained.get().getAmount());
                    queuedRequests.add(request);
                }

                finalLinkInOrder = true;
                finalPackageAtLink = true;
                orderContext = request.context();
                fixedOrderId = request.orderId();
                fixedAddress = request.address();
            } else {
                box = jarPackagerBE.extractJar(BoardIngredient.EMPTY);
            }

            if (box == ItemStack.EMPTY) return ItemStack.EMPTY;
        } else {
            IItemHandler targetInv = targetInventory.getInventory();
            if (targetInv == null || targetInv instanceof PackagerItemHandler)
                return ItemStack.EMPTY;

            boolean anyItemPresent = false;
            ItemStackHandler extractedItems = new ItemStackHandler(PackageItem.SLOTS);
            ItemStack extractedPackageItem = ItemStack.EMPTY;
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
            for (int i = 0; i < PackageItem.SLOTS; i++) {
                boolean continuePacking = true;

                while (continuePacking) {
                    continuePacking = false;

                    for (int slot = 0; slot < targetInv.getSlots(); slot++) {
                        int initialCount = requestQueue ? Math.min(64, nextRequest.getCount()) : 64;
                        ItemStack extracted = targetInv.extractItem(slot, initialCount, true);
                        if (extracted.isEmpty())
                            continue;
                        if (requestQueue &&
                                nextRequest.ingredient() instanceof ItemBoardIngredient itemBoardIngredient &&
                                !ItemHandlerHelper.canItemStacksStack(extracted, itemBoardIngredient.stack()))
                            continue;

                        boolean bulky = !extracted.getItem()
                                .canFitInsideContainerItems();
                        if (bulky && anyItemPresent)
                            continue;

                        anyItemPresent = true;
                        int leftovers = ItemHandlerHelper.insertItemStacked(extractedItems, extracted.copy(), false)
                                .getCount();
                        int transferred = extracted.getCount() - leftovers;
                        targetInv.extractItem(slot, transferred, false);

                        if (extracted.getItem() instanceof PackageItem)
                            extractedPackageItem = extracted;

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

            if (!anyItemPresent) {
                if (nextRequest != null)
                    queuedRequests.remove(0);
                return ItemStack.EMPTY;
            }

            box = extractedPackageItem.isEmpty() ? PackageItem.containing(extractedItems) : extractedPackageItem.copy();
        }

        PackageItem.clearAddress(box);

        if (fixedAddress != null)
            PackageItem.addAddress(box, fixedAddress);
        if (requestQueue)
            IngredientOrder.set(box, fixedOrderId, linkIndexInOrder, finalLinkInOrder, packageIndexAtLink, finalPackageAtLink, orderContext);
        if (!requestQueue && !signBasedAddress.isBlank())
            PackageItem.addAddress(box, signBasedAddress);

        return box;
    }

    @Override
    public BlockPos getLink() {
        return getLinkPos();
    }
}
