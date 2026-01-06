package ru.zznty.create_factory_abstractions.api.generic.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;

public interface GenericInventory {
    @Nullable GenericInventorySummaryProvider get(GenericKeyRegistration registration);

//    maybe in the future
//    GenericStack insert(GenericStack stack);

    static GenericInventory of(Level world, BlockPos pos) {
        GenericInventory capability = world.getCapability(AbstractionsCapabilities.GENERIC_INVENTORY, pos, null);
        if (capability != null) return capability;

        return registration -> {
            @Nullable GenericCapabilityWrapperProvider<Object, Object> provider = registration.provider().capabilityWrapperProvider();
            if (provider == null) return null;
            Object cap = world.getCapability(provider.capability(), pos, null);
            if (cap == null) return null;
            return provider.unwrap(cap);
        };
    }

    static GenericInventory of(ItemStack stack) {
        GenericInventory inventory = stack.getCapability(AbstractionsCapabilities.GENERIC_INVENTORY_ITEM);
        if (inventory != null) return inventory;

        return registration -> {
            @Nullable GenericCapabilityWrapperProvider<Object, Object> provider = registration.provider().capabilityWrapperProvider();
            if (provider == null) return null;
            Object cap = stack.getCapability(provider.capabilityItem());
            if (cap == null) return null;
            return provider.unwrap(cap);
        };
    }
}
