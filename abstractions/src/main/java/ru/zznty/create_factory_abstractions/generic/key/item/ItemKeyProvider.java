package ru.zznty.create_factory_abstractions.generic.key.item;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;
import ru.zznty.create_factory_abstractions.generic.impl.BuiltInPackageBuilder;

import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Internal
public class ItemKeyProvider implements GenericKeyProvider<ItemKey> {
    private final GenericCapabilityWrapperProvider<IItemHandler, IItemHandler> provider = new GenericCapabilityWrapperProvider<>() {
        @Override
        public BlockCapability<IItemHandler, Direction> capability() {
            return Capabilities.ItemHandler.BLOCK;
        }

        @Override
        public ItemCapability<IItemHandler, Void> capabilityItem() {
            return Capabilities.ItemHandler.ITEM;
        }

        @Override
        public IItemHandler wrap(GenericInventorySummaryProvider summaryProvider, HolderLookup.Provider registries) {
            return new NetworkItemHandler(summaryProvider, registries);
        }

        @Override
        public GenericInventorySummaryProvider unwrap(IItemHandler capability) {
            return new ItemInventorySummaryProvider(capability);
        }
    };

    @Override
    public ItemKey defaultKey() {
        return new ItemKey(ItemStack.EMPTY);
    }

    @Override
    public <T> ItemKey wrap(T value) {
        if (value instanceof ItemStack itemStack)
            return new ItemKey(itemStack.copyWithCount(1));
        throw new IllegalArgumentException("Expected ItemStack, got " + value.getClass());
    }

    @Override
    public <T> ItemKey wrapGeneric(T value) {
        if (value instanceof ItemKey itemKey)
            return itemKey;
        if (value instanceof ItemStack itemStack)
            return new ItemKey(new ItemStack(itemStack.getItemHolder()));
        throw new IllegalArgumentException("Expected ItemStack, got " + value.getClass());
    }

    @Override
    public <T> T unwrap(ItemKey key) {
        //noinspection unchecked
        return (T) key.stack();
    }

    @Override
    public String ingredientTypeUid(ItemKey key) {
        return "item_stack";
    }

    @Override
    public boolean supportsIngredientTypeUid(String uid) {
        return uid.equals("item_stack");
    }

    @Override
    public <T> Optional<ResourceKey<T>> resourceKey(ItemKey key) {
        //noinspection rawtypes
        Optional resourceKey = BuiltInRegistries.ITEM.getResourceKey(key.stack().getItem());
        //noinspection unchecked
        return resourceKey;
    }

    @Override
    public int compare(ItemKey o1, ItemKey o2) {
        return Integer.compare(o1.stack().getItem().hashCode(), o2.stack().getItem().hashCode());
    }

    @Override
    public @Nullable <Cap, ItemCap> GenericCapabilityWrapperProvider<Cap, ItemCap> capabilityWrapperProvider() {
        //noinspection unchecked
        return (GenericCapabilityWrapperProvider<Cap, ItemCap>) provider;
    }

    @Override
    public @Nullable Supplier<PackageBuilder> packageBuilder() {
        return BuiltInPackageBuilder::new;
    }

    @Override
    public int stackSize(ItemKey key) {
        return key.stack().getMaxStackSize();
    }

    @Override
    public int maxStackSize(ItemKey key) {
        return stackSize(key);
    }
}
