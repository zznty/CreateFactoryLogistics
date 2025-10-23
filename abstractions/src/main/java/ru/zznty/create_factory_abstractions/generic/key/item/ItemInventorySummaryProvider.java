package ru.zznty.create_factory_abstractions.generic.key.item;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

record ItemInventorySummaryProvider(IItemHandler handler) implements GenericInventorySummaryProvider {
    @Override
    public void apply(GenericInventorySummary summary) {
        if (handler instanceof BottomlessItemHandler bih) {
            summary.add(GenericStack.wrap(bih.getStackInSlot(0)).withAmount(BigItemStack.INF));
            return;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            summary.add(GenericStack.wrap(stack));
        }
    }
}
