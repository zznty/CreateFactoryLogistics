package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.CraftableIngredientStack;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftableBigItemStack.class)
public abstract class CraftableBigItemStackMixin implements CraftableIngredientStack {
    @Unique
    private List<BoardIngredient> createFactoryLogistics$ingredients = new ArrayList<>();

    @Override
    public List<BoardIngredient> ingredients() {
        return createFactoryLogistics$ingredients;
    }

    @Override
    public CraftableBigItemStack asStack() {
        return (CraftableBigItemStack) (Object) this;
    }
}
