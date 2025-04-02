package ru.zznty.create_factory_logistics.logistics.ingredient.capability;

import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;

import java.util.List;

public interface PackageBuilder {
    /**
     * Adds an ingredient to a future package and returns leftover amount
     *
     * @param content The content to add
     * @return Leftover amount or negative if the package content cannot be stacked with existing ingredients
     */
    int add(BoardIngredient content);

    /**
     * @return Returns an immutable list of ingredients in the future package
     */
    List<BoardIngredient> content();

    /**
     * Checks if the package builder is full
     * If the package builder is full, no more ingredients would be accepted
     *
     * @return Whether the package builder is full
     */
    boolean isFull();

    /**
     * Returns generic max amount of ingredient per slot in the future package
     *
     * @return Generic max amount
     */
    int maxPerSlot();

    /**
     * Returns the number of inventory slots in the future package
     *
     * @return Slot count
     */
    int slotCount();

    /**
     * Measures the ingredient for the future package
     *
     * @param key Ingredient key to measure
     * @return Measurement result
     */
    PackageMeasureResult measure(IngredientKey key);

    /**
     * Builds the package
     *
     * @return The built package or empty if the package is empty
     */
    ItemStack build();
}
