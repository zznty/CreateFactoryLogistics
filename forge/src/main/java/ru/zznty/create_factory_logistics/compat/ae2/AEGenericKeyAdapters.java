package ru.zznty.create_factory_logistics.compat.ae2;

import appeng.api.stacks.AEKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class AEGenericKeyAdapters {
    private static final Map<ResourceLocation, AEGenericKeyAdapter> adapters = new HashMap<>();

    public static void register(ResourceLocation key, AEGenericKeyAdapter adapter) {
        adapters.put(key, adapter);
    }

    @Nullable
    public static AEGenericKeyAdapter get(GenericKeyRegistration registration) {
        ResourceLocation key = GenericContentExtender.REGISTRY.getKey(registration);
        if (key == null) return null;
        return adapters.get(key);
    }

    @Nullable
    public static AEKey toAEKey(GenericStack stack) {
        GenericKeyRegistration reg = GenericContentExtender.registrationOf(stack.key());
        if (reg == null) return null;
        AEGenericKeyAdapter adapter = get(reg);
        if (adapter == null) return null;
        return adapter.toAEKey(stack.key(), stack.amount());
    }

    @Nullable
    public static GenericStack toGenericStack(AEKey key, long amount) {
        for (AEGenericKeyAdapter adapter : adapters.values()) {
            GenericStack result = adapter.toGenericStack(key, amount);
            if (result != null) return result;
        }
        return null;
    }

    private AEGenericKeyAdapters() {}
}
