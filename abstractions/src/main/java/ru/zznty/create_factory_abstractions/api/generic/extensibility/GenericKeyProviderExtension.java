package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import net.minecraft.resources.ResourceKey;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

public interface GenericKeyProviderExtension<Key extends GenericKey, Value, RegistryValue, Capability, ItemCapability> extends Comparator<Key> {
    Key defaultKey();

    Key wrap(Value value);

    Key wrapGeneric(Value value);

    Value unwrap(Key key);

    String ingredientTypeUid(Key key);

    boolean supportsIngredientTypeUid(String uid);

    Optional<ResourceKey<RegistryValue>> resourceKey(Key key);

    GenericCapabilityWrapperProvider<Capability, ItemCapability> capabilityWrapperProvider();

    Supplier<PackageBuilder> packageBuilder();

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
