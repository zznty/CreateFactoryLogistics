package ru.zznty.create_factory_logistics.logistics.repackager;

import com.google.common.collect.Lists;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.ArrayList;
import java.util.List;

public class FactoryRepackagerHelper extends PackageRepackageHelper {
    protected final RepackagerBlockEntity blockEntity;

    public FactoryRepackagerHelper(RepackagerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public List<BigItemStack> repack(int orderId, RandomSource r) {
        List<BigItemStack> exportingPackages = new ArrayList<>();
        String address = "";
        PackageOrderWithCrafts orderContext = null;
        GenericOrder order = null;
        InventorySummary summary = new InventorySummary();

        for (ItemStack box : collectedPackages.get(orderId)) {
            address = PackageItem.getAddress(box);
            order = GenericOrder.of(blockEntity.getLevel().registryAccess(), box);
            if (order != null && !order.isEmpty()) {
                orderContext = order.asCrafting();
            }

            ItemStackHandler contents = PackageItem.getContents(box);
            for (int slot = 0; slot < contents.getSlots(); slot++)
                summary.add(contents.getStackInSlot(slot));
        }

        List<BigItemStack> orderedStacks = new ArrayList<>();
        if (orderContext != null) {
            List<BigItemStack> packagesSplitByRecipe = repackBasedOnRecipes(summary, orderContext, address, r);
            exportingPackages.addAll(packagesSplitByRecipe);

            if (packagesSplitByRecipe.isEmpty())
                for (BigItemStack stack : orderContext.stacks())
                    orderedStacks.add(new BigItemStack(stack.stack, stack.count));
        }

        List<BigItemStack> allItems = summary.getStacks();
        List<ItemStack> outputSlots = new ArrayList<>();

        Repack:
        while (true) {
            allItems.removeIf(e -> e.count == 0);
            if (allItems.isEmpty())
                break;

            BigItemStack targetedEntry = null;
            if (!orderedStacks.isEmpty())
                targetedEntry = orderedStacks.remove(0);

            ItemSearch:
            for (BigItemStack entry : allItems) {
                int targetAmount = entry.count;
                if (targetAmount == 0)
                    continue;
                if (targetedEntry != null) {
                    targetAmount = targetedEntry.count;
                    if (!ItemStack.isSameItemSameComponents(entry.stack, targetedEntry.stack))
                        continue;
                }

                while (targetAmount > 0) {
                    int removedAmount = Math.min(Math.min(targetAmount, entry.stack.getMaxStackSize()), entry.count);
                    if (removedAmount == 0)
                        continue ItemSearch;

                    ItemStack output = entry.stack.copyWithCount(removedAmount);
                    targetAmount -= removedAmount;
                    if (targetedEntry != null)
                        targetedEntry.count = targetAmount;
                    entry.count -= removedAmount;
                    outputSlots.add(output);
                }

                continue Repack;
            }
        }

        int currentSlot = 0;
        ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);

        for (ItemStack item : outputSlots) {
            target.setStackInSlot(currentSlot++, item);
            if (currentSlot < PackageItem.SLOTS)
                continue;
            exportingPackages.add(new BigItemStack(PackageItem.containing(target), 1));
            target = new ItemStackHandler(PackageItem.SLOTS);
            currentSlot = 0;
        }

        for (int slot = 0; slot < target.getSlots(); slot++)
            if (!target.getStackInSlot(slot)
                    .isEmpty()) {
                exportingPackages.add(new BigItemStack(PackageItem.containing(target), 1));
                break;
            }

        for (BigItemStack box : exportingPackages)
            PackageItem.addAddress(box.stack, address);

        for (int i = 0; i < exportingPackages.size(); i++) {
            BigItemStack box = exportingPackages.get(i);
            boolean isfinal = i == exportingPackages.size() - 1;
            GenericOrder outboundOrderContext = isfinal && order != null ? order : null;
            if (PackageItem.getOrderId(box.stack) == -1)
                GenericOrder.set(blockEntity.getLevel().registryAccess(), box.stack, orderId, 0, true, 0, true,
                                 outboundOrderContext);
        }

        return exportingPackages;
    }

    @Override
    protected List<BigItemStack> repackBasedOnRecipes(InventorySummary summary, PackageOrderWithCrafts order,
                                                      String address, RandomSource r) {
        if (order.orderedCrafts().isEmpty())
            return List.of();

        List<BigItemStack> packages = new ArrayList<>();
        for (PackageOrderWithCrafts.CraftingEntry craftingEntry : order.orderedCrafts()) {
            int packagesToCreate = 0;
            Crafts:
            for (int i = 0; i < craftingEntry.count(); i++) {
                for (BigItemStack required : craftingEntry.pattern().stacks()) {
                    if (required.stack.isEmpty())
                        continue;
                    if (summary.getCountOf(required.stack) <= 0)
                        break Crafts;
                    summary.add(required.stack, -1);
                }
                packagesToCreate++;
            }

            ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);
            List<BigItemStack> stacks = craftingEntry.pattern().stacks();
            for (int currentSlot = 0; currentSlot < Math.min(stacks.size(), target.getSlots()); currentSlot++)
                target.setStackInSlot(currentSlot, stacks.get(currentSlot).stack.copyWithCount(1));

            ItemStack box = PackageItem.containing(target);
            GenericOrder.set(blockEntity.getLevel().registryAccess(), box, r.nextInt(), 0, true, 0, true,
                             GenericOrder.of(PackageOrderWithCrafts.singleRecipe(craftingEntry.pattern()
                                                                                         .stacks())));
            packages.add(new BigItemStack(box, packagesToCreate));
        }

        return packages;
    }

    @Override
    public boolean isFragmented(ItemStack box) {
        return box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("Fragment");
    }

    @Override
    public int addPackageFragment(ItemStack box) {
        int collectedOrderId = PackageItem.getOrderId(box);
        if (collectedOrderId == -1)
            return -1;

        List<ItemStack> collectedOrder = collectedPackages.computeIfAbsent(collectedOrderId, $ -> Lists.newArrayList());
        collectedOrder.add(box);

        if (!isOrderComplete(collectedOrderId))
            return -1;

        return collectedOrderId;
    }

    private boolean isOrderComplete(int orderId) {
        boolean finalLinkReached = false;
        Links:
        for (int linkCounter = 0; linkCounter < 1000; linkCounter++) {
            if (finalLinkReached)
                break;
            Packages:
            for (int packageCounter = 0; packageCounter < 1000; packageCounter++) {
                for (ItemStack box : collectedPackages.get(orderId)) {
                    //noinspection deprecation
                    CompoundTag fragment = box.getOrDefault(DataComponents.CUSTOM_DATA,
                                                            CustomData.EMPTY).getUnsafe().getCompound("Fragment");
                    if (linkCounter != fragment.getInt("LinkIndex"))
                        continue;
                    if (packageCounter != fragment.getInt("Index"))
                        continue;
                    finalLinkReached = fragment.getBoolean("IsFinalLink");
                    if (fragment.getBoolean("IsFinal"))
                        continue Links;
                    continue Packages;
                }
                return false;
            }
        }
        return true;
    }
}
