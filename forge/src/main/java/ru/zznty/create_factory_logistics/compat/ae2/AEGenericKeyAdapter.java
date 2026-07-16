package ru.zznty.create_factory_logistics.compat.ae2;

import appeng.api.stacks.AEKey;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public interface AEGenericKeyAdapter {
    @Nullable
    AEKey toAEKey(GenericKey key, int amount);

    @Nullable
    GenericStack toGenericStack(AEKey key, long amount);
}
