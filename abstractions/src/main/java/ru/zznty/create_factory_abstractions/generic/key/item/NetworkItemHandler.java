package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.List;

final class NetworkItemHandler implements IItemHandler {

    private final List<GenericStack> stacks;

    public NetworkItemHandler(GenericInventorySummaryProvider summaryProvider) {
        GenericInventorySummary summary = GenericInventorySummary.empty();
        summaryProvider.apply(false, summary);
        stacks = summary.get().stream().filter(s -> s.key() instanceof ItemKey).toList();
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return asItem(stacks.get(slot));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
        // causes too much trouble with other mods implementing extraction without any checks
//        if (!simulate) return ItemStack.EMPTY;
//
//        ItemStack stack = getStackInSlot(slot);
//
//        return stack.copyWithCount(Math.min(stack.getCount(), amount));
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE - 1 / getSlots();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return ItemHandlerHelper.canItemStacksStack(getStackInSlot(slot), stack);
    }

    private static ItemStack asItem(GenericStack stack) {
        if (stack.key() instanceof ItemKey itemKey)
            return itemKey.stack().copyWithCount(stack.amount());
        return ItemStack.EMPTY;
    }
}
