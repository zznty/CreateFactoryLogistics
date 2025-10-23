package ru.zznty.create_factory_abstractions.api.generic.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;

@AutoRegisterCapability
public interface GenericInventory {
    @Nullable GenericInventorySummaryProvider get(GenericKeyRegistration registration);

//    maybe in the future
//    GenericStack insert(GenericStack stack);

    static GenericInventory of(ICapabilityProvider capProvider) {
        return capProvider.getCapability(AbstractionsCapabilities.GENERIC_INVENTORY).orElseGet(() -> registration -> {
            @Nullable GenericCapabilityWrapperProvider<Object> provider = registration.provider().capabilityWrapperProvider();
            if (provider == null) return null;
            return capProvider.getCapability(provider.capability()).map(provider::unwrap).orElse(null);
        });
    }
}
