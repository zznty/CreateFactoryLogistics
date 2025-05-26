package ru.zznty.create_factory_abstractions.api.generic.stack;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.ArrayList;
import java.util.List;

public interface GenericIngredient {
    boolean isEmpty();

    int amount();

    boolean test(GenericStack stack);

    static GenericIngredient of(GenericStack genericStack) {
        return new GenericIngredient() {
            private final GenericStack stack = genericStack;

            @Override
            public boolean isEmpty() {
                return stack.isEmpty();
            }

            @Override
            public int amount() {
                return stack.amount();
            }

            @Override
            public boolean test(GenericStack stack) {
                return this.stack.canStack(stack);
            }
        };
    }

    static List<GenericIngredient> ofRecipe(Recipe<?> recipe) {
        NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
        ArrayList<GenericIngredient> ingredients = new ArrayList<>(recipeIngredients.size());

        for (Ingredient recipeIngredient : recipeIngredients) {
            ingredients.add(new GenericIngredient() {
                private final Ingredient ingredient = recipeIngredient;

                @Override
                public boolean isEmpty() {
                    return ingredient.isEmpty();
                }

                @Override
                public int amount() {
                    return 1;
                }

                @Override
                public boolean test(GenericStack stack) {
                    if (stack.key() instanceof ItemKey itemKey)
                        return ingredient.test(itemKey.stack());
                    return false;
                }
            });
        }

        return ingredients;
    }
}
