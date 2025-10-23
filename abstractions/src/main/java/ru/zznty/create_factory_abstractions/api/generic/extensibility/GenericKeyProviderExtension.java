package ru.zznty.create_factory_abstractions.api.generic.extensibility;

import net.minecraft.resources.ResourceKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

import java.util.Comparator;
import java.util.Optional;

public interface GenericKeyProviderExtension<Key extends GenericKey, Value, RegistryValue, Capability> extends Comparator<Key> {
    Key defaultKey();

    Key wrap(Value value);

    Key wrapGeneric(Value value);

    Value unwrap(Key key);

    String ingredientTypeUid();

    Optional<ResourceKey<RegistryValue>> resourceKey(Key key);

    GenericCapabilityWrapperProvider<Capability> capabilityWrapperProvider();
}
