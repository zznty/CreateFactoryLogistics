package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericIngredient;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.List;

public interface CraftableGenericStack extends BigGenericStack {
    List<GenericIngredient> ingredients();

    List<GenericStack> results(RegistryAccess registryAccess);

    int outputCount(Level level);

    CraftableBigItemStack asStack();

    static CraftableGenericStack of(CraftableBigItemStack stack) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return (CraftableGenericStack) stack;

        return new CraftableGenericStack() {
            private final CraftableBigItemStack itemStack = stack;

            @Override
            public List<GenericIngredient> ingredients() {
                return GenericIngredient.ofRecipe(itemStack.recipe);
            }

            @Override
            public List<GenericStack> results(RegistryAccess registryAccess) {
                return List.of(GenericStack.wrap(itemStack.recipe.getResultItem(registryAccess)));
            }

            @Override
            public int outputCount(Level level) {
                return itemStack.getOutputCount(level);
            }

            @Override
            public CraftableBigItemStack asStack() {
                return itemStack;
            }

            @Override
            public GenericStack get() {
                return GenericStack.wrap(itemStack.stack).withAmount(itemStack.count);
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
        };
    }
}
