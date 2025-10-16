package ru.zznty.create_factory_abstractions.generic.impl;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.ArrayList;
import java.util.List;

class BuiltInPackageBuilder implements PackageBuilder {
    private ItemStackHandler inventory = new ItemStackHandler(PackageItem.SLOTS);
    private boolean hasBulky = false;
    private boolean isEmpty = true;

    @Override
    public int add(GenericStack content) {
        if (hasBulky) return -1;

        if (content.key() instanceof ItemKey itemKey) {
            hasBulky = measure(itemKey) == PackageMeasureResult.BULKY;

            ItemStack stack = itemKey.stack();
            if (PackageItem.isPackage(stack)) {
                // TODO figure out a better check if this is an items package
                // just strict class comparison for PackageItem wont fit since mods could extend it for their new packages
                if (stack.getTagElement("Items") == null || stack.getTagElement("Items").isEmpty()) return -1;

                if (!isEmpty) return -1;

                // assume if we have a complete package that we can fit it
                inventory = PackageItem.getContents(stack);
                isEmpty = false;
                return 0;
            }

            int result = ItemHandlerHelper.insertItemStacked(inventory, stack.copyWithCount(content.amount()),
                                                             false).getCount();
            if (result != content.amount()) isEmpty = false;
            return result;
        }

        throw new IllegalArgumentException("Unsupported content: " + content);
    }

    @Override
    public List<GenericStack> content() {
        List<GenericStack> list = new ArrayList<>();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;

            list.add(GenericStack.wrap(stackInSlot));
        }

        return list;
    }

    @Override
    public boolean isFull() {
        if (hasBulky) return true;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty() || stackInSlot.getCount() < stackInSlot.getMaxStackSize()) return false;
        }

        return true;
    }

    @Override
    public int maxPerSlot() {
        return Item.MAX_STACK_SIZE;
    }

    @Override
    public int slotCount() {
        return PackageItem.SLOTS;
    }

    @Override
    public PackageMeasureResult measure(GenericKey key) {
        if (key instanceof ItemKey itemKey)
            return itemKey.stack().getItem().canFitInsideContainerItems() ?
                   PackageMeasureResult.REGULAR :
                   PackageMeasureResult.BULKY;

        throw new IllegalArgumentException("Unsupported key: " + key);
    }

    @Override
    public ItemStack build() {
        return inventory.getStackInSlot(0).isEmpty() ? ItemStack.EMPTY : PackageItem.containing(inventory);
    }
}
