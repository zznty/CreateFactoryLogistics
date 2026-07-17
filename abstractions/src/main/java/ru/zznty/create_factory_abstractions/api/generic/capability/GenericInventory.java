package ru.zznty.create_factory_abstractions.api.generic.capability;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.ArrayList;
import java.util.List;

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

        if (stack.has(AllDataComponents.PACKAGE_CONTENTS))
            return registration -> registration == GenericContentExtender.REGISTRATIONS.get(ItemKey.class)
                ? new ItemInventorySummaryProvider(PackageItem.getContents(stack)) : null;

        return registration -> {
            @Nullable GenericCapabilityWrapperProvider<Object, Object> provider = registration.provider().capabilityWrapperProvider();
            if (provider == null) return null;
            Object cap = stack.getCapability(provider.capabilityItem());
            if (cap == null) return null;
            return provider.unwrap(cap);
        };
    }

    static List<GenericStack> collectStacks(ItemStack stack, HolderLookup.Provider registries) {
        GenericInventory inventory = of(stack);
        GenericInventorySummary summary = GenericInventorySummary.empty();
        for (GenericKeyRegistration registration : GenericContentExtender.REGISTRATIONS.values()) {
            GenericInventorySummaryProvider provider = inventory.get(registration);
            if (provider != null)
                provider.apply(summary, registries);
        }
        return summary.get();
    }
}
