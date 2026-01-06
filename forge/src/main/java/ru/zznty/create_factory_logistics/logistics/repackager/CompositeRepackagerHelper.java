package ru.zznty.create_factory_logistics.logistics.repackager;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventoryHelper;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CompositeRepackagerHelper extends FactoryRepackagerHelper {

    public CompositeRepackagerHelper(RepackagerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public List<BigItemStack> repack(int orderId, RandomSource r) {
        List<BigItemStack> exportingPackages = new ArrayList<>();
        String address = "";
        GenericOrder orderContext = null;
        GenericInventorySummary summary = GenericInventorySummary.empty();

        for (ItemStack box : collectedPackages.get(orderId)) {
            address = PackageItem.getAddress(box);
            CustomData tagData = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (tagData.contains("Fragment")) {
                CompoundTag tag = tagData.getUnsafe().getCompound("Fragment");
                if (tag.contains("OrderContext"))
                    orderContext = GenericOrder.read(blockEntity.getLevel().registryAccess(), tag.getCompound("OrderContext"));
            }

            GenericInventory inventory = GenericInventory.of(box);
            if (inventory != null)
                GenericInventoryHelper.fillSummary(inventory, summary, blockEntity.getLevel().registryAccess());
        }

        List<GenericStack> orderedStacks = new ArrayList<>();
        if (orderContext != null && !orderContext.crafts().isEmpty()) {
            List<BigItemStack> packagesSplitByRecipe = repackBasedOnRecipes(summary.asSummary(),
                                                                            orderContext.asCrafting(), address,
                                                                            r);
            exportingPackages.addAll(packagesSplitByRecipe);

            if (packagesSplitByRecipe.isEmpty())
                orderedStacks.addAll(orderContext.stacks());
        }

        List<GenericStack> allStacks = summary.get();
        GenericInventorySummary outputSummary = GenericInventorySummary.empty();

        Repack:
        while (true) {
            if (allStacks.isEmpty())
                break;

            GenericStack targetedEntry = null;
            if (!orderedStacks.isEmpty())
                targetedEntry = orderedStacks.remove(0);

            for (int i = 0; i < allStacks.size(); i++) {
                GenericStack entry = allStacks.get(i);
                int targetAmount = entry.amount();
                if (targetAmount == 0)
                    continue;
                if (targetedEntry != null) {
                    targetAmount = targetedEntry.amount();
                    if (!entry.canStack(targetedEntry))
                        continue;
                }

                while (targetAmount > 0) {
                    int maxStackSize = GenericContentExtender.registrationOf(entry.key()).provider().maxStackSize(
                            entry.key());
                    int removedAmount = Math.min(maxStackSize > 0 ? Math.min(targetAmount, maxStackSize) : targetAmount,
                                                 entry.amount());

                    GenericStack output = entry.withAmount(removedAmount);
                    targetAmount -= removedAmount;
                    if (targetedEntry != null)
                        targetedEntry = targetedEntry.withAmount(targetAmount);
                    allStacks.set(i, entry.withAmount(entry.amount() - removedAmount));
                    if (allStacks.get(i).isEmpty())
                        allStacks.remove(i);
                    outputSummary.add(output);
                }

                continue Repack;
            }
        }


        List<GenericStack> outputStacks = outputSummary.get();

        List<BigItemStack> itemBoxes = new ArrayList<>();
        partitionStacksIntoPackages(outputStacks, itemBoxes);

        List<GenericStack> nonItemStacks = new ArrayList<>(
                outputStacks.stream().filter(stack -> !(stack.key() instanceof ItemKey)).toList());
        while (!itemBoxes.isEmpty()) {
            BigItemStack itemBox = itemBoxes.remove(itemBoxes.size() - 1);

            ItemStack[] children = new ItemStack[Iterate.horizontalDirections.length];

            for (int i = 0; i < Iterate.horizontalDirections.length && !nonItemStacks.isEmpty(); i++) {
                GenericStack stack = nonItemStacks.remove(nonItemStacks.size() - 1);
                PackageBuilder builder = createBuilder(stack);
                int remaining = builder.add(stack);
                if (remaining > 0)
                    nonItemStacks.add(stack.withAmount(remaining));
                children[i] = builder.build();
            }

            List<ItemStack> wrapped = Arrays.stream(children).filter(Objects::nonNull).toList();
            exportingPackages.add(new BigItemStack(CompositePackageItem.of(blockEntity.getLevel().registryAccess(), itemBox.stack, wrapped), 1));
        }

        // package all remaining stacks
        while (!nonItemStacks.isEmpty()) {
            List<ItemStack> children = new ArrayList<>();
            for (int i = 0; i < Iterate.horizontalDirections.length && !nonItemStacks.isEmpty(); i++) {
                GenericStack stack = nonItemStacks.remove(nonItemStacks.size() - 1);
                PackageBuilder builder = createBuilder(stack);
                int remaining = builder.add(stack);
                if (remaining > 0)
                    nonItemStacks.add(stack.withAmount(remaining));
                children.add(builder.build());
            }

            if (children.size() == 1 && itemBoxes.isEmpty()) {
                exportingPackages.add(new BigItemStack(children.get(0), 1));
            } else {
                exportingPackages.add(new BigItemStack(CompositePackageItem.of(blockEntity.getLevel().registryAccess(), ItemStack.EMPTY, children), 1));
            }
        }

        for (BigItemStack box : exportingPackages)
            PackageItem.addAddress(box.stack, address);

        for (int i = 0; i < exportingPackages.size(); i++) {
            BigItemStack box = exportingPackages.get(i);
            boolean isfinal = i == exportingPackages.size() - 1;
            GenericOrder outboundOrderContext = isfinal && orderContext != null ? orderContext : null;
            if (PackageItem.getOrderId(box.stack) == -1)
                GenericOrder.set(blockEntity.getLevel().registryAccess(), box.stack, orderId, 0, true, 0, true, outboundOrderContext);
        }

        return exportingPackages;
    }

    private static void partitionStacksIntoPackages(List<GenericStack> outputStacks,
                                                    List<BigItemStack> exportingPackages) {
        int currentSlot = 0;
        ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);
        for (GenericStack stack : outputStacks) {
            if (!(stack.key() instanceof ItemKey itemKey))
                continue;
            target.setStackInSlot(currentSlot++, itemKey.stack().copyWithCount(stack.amount()));
            if (currentSlot < PackageItem.SLOTS)
                continue;
            exportingPackages.add(new BigItemStack(PackageItem.containing(target), 1));
            target = new ItemStackHandler(PackageItem.SLOTS);
            currentSlot = 0;
        }
        if (currentSlot > 0)
            exportingPackages.add(new BigItemStack(PackageItem.containing(target), 1));
    }

    @Nullable
    private static PackageBuilder createBuilder(GenericStack stack) {
        Supplier<PackageBuilder> supplier = GenericContentExtender.registrationOf(stack.key()).provider().packageBuilder();
        return supplier != null ? supplier.get() : null;
    }
}
