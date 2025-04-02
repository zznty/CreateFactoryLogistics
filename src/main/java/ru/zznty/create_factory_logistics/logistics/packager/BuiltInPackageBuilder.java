package ru.zznty.create_factory_logistics.logistics.packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageBuilder;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageMeasureResult;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemIngredientKey;

import java.util.ArrayList;
import java.util.List;

class BuiltInPackageBuilder implements PackageBuilder {
    private final ItemStackHandler inventory = new ItemStackHandler(PackageItem.SLOTS);
    private boolean hasBulky = false;

    @Override
    public int add(BoardIngredient content) {
        if (hasBulky) return -1;


        if (content.key() instanceof ItemIngredientKey itemKey) {
            hasBulky = itemKey.stack().getItem().canFitInsideContainerItems();

            return ItemHandlerHelper.insertItemStacked(inventory, itemKey.stack(), false).getCount();
        }

        throw new IllegalArgumentException("Unsupported content: " + content);
    }

    @Override
    public List<BoardIngredient> content() {
        List<BoardIngredient> list = new ArrayList<>();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;

            list.add(new BoardIngredient(IngredientKey.of(stackInSlot), stackInSlot.getCount()));
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
    public PackageMeasureResult measure(IngredientKey key) {
        if (key instanceof ItemIngredientKey itemKey)
            return itemKey.stack().getItem().canFitInsideContainerItems() ? PackageMeasureResult.REGULAR : PackageMeasureResult.BULKY;

        throw new IllegalArgumentException("Unsupported key: " + key);
    }

    @Override
    public ItemStack build() {
        return inventory.getStackInSlot(0).isEmpty() ? ItemStack.EMPTY : PackageItem.containing(inventory);
    }
}
