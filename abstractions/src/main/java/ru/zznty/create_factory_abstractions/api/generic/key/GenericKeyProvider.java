package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

public interface GenericKeyProvider<Key extends GenericKey> extends Comparator<Key> {
    Key defaultKey();

    <T> Key wrap(T value);

    <T> Key wrapGeneric(T value);

    <T> T unwrap(Key key);

    String ingredientTypeUid(Key key);

    boolean supportsIngredientTypeUid(String uid);

    <T> Optional<ResourceKey<T>> resourceKey(Key key);

    default <Cap, ItemCap> @Nullable GenericCapabilityWrapperProvider<Cap, ItemCap> capabilityWrapperProvider() {
        return null;
    }

    default @Nullable Supplier<PackageBuilder> packageBuilder() {
        return null;
    }

    /**
     * Returns stack (shift-click) size of a key
     */
    int stackSize(Key key);

    /**
     * Returns max stack (shift-click) size of a key
     * Return negative value if size is unlimited e.g. fluids
     */
    int maxStackSize(Key key);
}
