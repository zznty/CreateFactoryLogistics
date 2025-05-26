package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.Comparator;

// mostly a dummy over GenericStack so dont bother with it if not required
public interface BigGenericStack {
    GenericStack get();

    @ApiStatus.Internal
        // not very useful, just use of(stack)
    void set(GenericStack stack);

    void setAmount(int amount);

    BigItemStack asStack();

    static BigGenericStack of(BigItemStack stack) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return (BigGenericStack) stack;

        return new BigGenericStack() {
            private final BigItemStack itemStack = stack;

            @Override
            public GenericStack get() {
                return GenericStack.wrap(itemStack.stack);
            }

            @Override
            public void set(GenericStack stack) {
                if (stack.key() instanceof ItemKey itemKey) itemStack.stack = itemKey.stack();
                else itemStack.stack = ItemStack.EMPTY;
                itemStack.count = stack.amount();
            }

            @Override
            public void setAmount(int amount) {
                itemStack.count = amount;
            }

            @Override
            public BigItemStack asStack() {
                return itemStack;
            }
        };
    }

    static BigGenericStack of(GenericStack stack) {
        BigGenericStack bigStack = of(new BigItemStack(ItemStack.EMPTY));
        bigStack.set(stack);
        return bigStack;
    }

    Comparator<BigGenericStack> COMPARATOR = (a, b) -> {
        int result = Integer.compare(b.get().amount(), a.get().amount());
        if (result != 0) return result;
        return GenericKey.COMPARATOR.compare(a.get().key(), b.get().key());
    };
}
