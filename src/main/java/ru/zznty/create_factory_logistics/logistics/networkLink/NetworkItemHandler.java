package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.UUID;

public class NetworkItemHandler extends BaseNetworkHandler implements IItemHandler {

    public NetworkItemHandler(UUID network, NetworkLinkMode mode) {
        super(network, mode);
    }

    @Override
    public int getSlots() {
        return summary().size();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return asItem(summary().get(slot));
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
        return ItemHelper.canItemStackAmountsStack(asItem(summary().get(slot)), stack);
    }

    private static ItemStack asItem(GenericStack stack) {
        if (stack.key() instanceof ItemKey itemKey)
            return itemKey.stack().copyWithCount(stack.amount());
        return ItemStack.EMPTY;
    }
}
