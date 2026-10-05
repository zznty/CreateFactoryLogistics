package ru.zznty.create_factory_abstractions.api.generic.key;

import com.simibubi.create.api.registry.SimpleRegistry;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class GenericKeyCanonicalizer {
    private static final SimpleRegistry<Class<?>, UnaryOperator<GenericKey>> BY_TYPE = SimpleRegistry.create();
    private static final Map<HolderPair, UnaryOperator<GenericKey>> BY_HOLDER = new HashMap<>();

    public static <K extends GenericKey> void register(Class<K> keyType, UnaryOperator<K> canonicalizer) {
        @SuppressWarnings("unchecked")
        UnaryOperator<GenericKey> cast = (UnaryOperator<GenericKey>) (UnaryOperator<?>) canonicalizer;
        BY_TYPE.register(keyType, cast);
    }

    public static <K extends GenericKey> void register(Class<K> keyType, ResourceKey<?> holderKey,
                                                        UnaryOperator<K> canonicalizer) {
        @SuppressWarnings("unchecked")
        UnaryOperator<GenericKey> cast = (UnaryOperator<GenericKey>) (UnaryOperator<?>) canonicalizer;
        if (BY_HOLDER.putIfAbsent(new HolderPair(keyType, holderKey), cast) != null)
            throw new IllegalArgumentException(
                    String.format("Tried to register duplicate canonicalizer for %s (%s)", holderKey, keyType));
    }

    @SuppressWarnings("unchecked")
    static <K extends ConcreteGenericKey<?>> GenericKey canonicalize(K key) {
        ResourceKey<?> holderKey = key.holder().getKey();
        if (holderKey != null) {
            UnaryOperator<GenericKey> fn = BY_HOLDER.get(new HolderPair(key.getClass(), holderKey));
            if (fn != null) return fn.apply(key);
        }
        UnaryOperator<GenericKey> fn = BY_TYPE.get(key.getClass());
        return fn != null ? fn.apply(key) : key;
    }

    private record HolderPair(Class<?> keyType, ResourceKey<?> holderKey) {}

    private GenericKeyCanonicalizer() {}
}
