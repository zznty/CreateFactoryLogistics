package ru.zznty.create_factory_abstractions.api.generic.key;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class ConcreteGenericKey<T> implements GenericKey {
    private static final Interner<GenericKey> CANONICAL_INTERN = Interners.newWeakInterner();

    protected final Holder<T> holder;
    protected final PatchedDataComponentMap nbt;
    private volatile GenericKey cachedCanonical;

    protected ConcreteGenericKey(Holder<T> holder, @Nullable PatchedDataComponentMap nbt) {
        this.holder = holder;
        this.nbt = nbt != null ? nbt : new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public Holder<T> holder() {
        return holder;
    }

    public PatchedDataComponentMap nbt() {
        return nbt;
    }

    public GenericKey canonical() {
        GenericKey c = cachedCanonical;
        if (c != null) return c;
        c = GenericKeyCanonicalizer.canonicalize(this);
        if (c instanceof ConcreteGenericKey<?> concrete)
            c = CANONICAL_INTERN.intern(new DefaultCanonical<>(concrete.holder.getKey(), concrete.nbt));
        return (cachedCanonical = c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcreteGenericKey<?> other)) return false;
        if (!Objects.equals(holder.getKey(), other.holder.getKey())) return false;
        return canonical().equals(other.canonical());
    }

    @Override
    public int hashCode() {
        return canonical().hashCode();
    }

    private record DefaultCanonical<T>(@Nullable ResourceKey<T> key, PatchedDataComponentMap nbt) implements GenericKey {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof DefaultCanonical(ResourceKey<?> keyB, PatchedDataComponentMap nbtB))
                return Objects.equals(key, keyB) && nbt.equals(nbtB);
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, nbt);
        }
    }
}
