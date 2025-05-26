package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericIngredient;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftableBigItemStack.class)
public abstract class CraftableBigItemStackMixin implements CraftableGenericStack {
    @Unique
    private final List<GenericIngredient> createFactoryLogistics$ingredients = new ArrayList<>();

    @Unique
    private final List<GenericStack> createFactoryLogistics$results = new ArrayList<>();

    @Override
    public List<GenericIngredient> ingredients() {
        return createFactoryLogistics$ingredients;
    }

    @Override
    public List<GenericStack> results(RegistryAccess registryAccess) {
        return createFactoryLogistics$results;
    }

    @Override
    public int outputCount(Level level) {
        int outputCount = asStack().getOutputCount(level);
        if (outputCount > 0) return outputCount;
        if (results(level.registryAccess()).isEmpty())
            throw new IllegalStateException("No results for " + this);
        return results(level.registryAccess()).get(0).amount();
    }

    @Override
    public CraftableBigItemStack asStack() {
        return (CraftableBigItemStack) (Object) this;
    }
}
