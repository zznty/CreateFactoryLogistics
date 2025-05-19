package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.CraftableIngredientStack;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftableBigItemStack.class)
public abstract class CraftableBigItemStackMixin implements CraftableIngredientStack {
    @Unique
    private final List<BoardIngredient> createFactoryLogistics$ingredients = new ArrayList<>();

    @Unique
    private final List<BoardIngredient> createFactoryLogistics$results = new ArrayList<>();

    @Override
    public List<BoardIngredient> ingredients() {
        return createFactoryLogistics$ingredients;
    }

    @Override
    public List<BoardIngredient> results() {
        return createFactoryLogistics$results;
    }

    @Override
    public int outputCount(Level level) {
        int outputCount = asStack().getOutputCount(level);
        if (outputCount > 0) return outputCount;
        if (results().isEmpty())
            throw new IllegalStateException("No results for " + this);
        return results().get(0).amount();
    }

    @Override
    public CraftableBigItemStack asStack() {
        return (CraftableBigItemStack) (Object) this;
    }
}
