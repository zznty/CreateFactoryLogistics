package ru.zznty.create_factory_abstractions.api.generic.capability;

import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public interface PackageBuilder {
    /**
     * Adds a stack to a future package and returns leftover amount
     *
     * @param content The content to add
     * @return Leftover amount or negative if the package content cannot be stacked with existing ingredients
     */
    int add(GenericStack content);

    /**
     * @return Returns an immutable list of stacks in the future package
     */
    List<GenericStack> content();

    /**
     * Checks if the package builder is full
     * If the package builder is full, no more stacks would be accepted
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
     * Measures the key for the future package
     *
     * @param key Generic key to measure
     * @return Measurement result
     */
    PackageMeasureResult measure(GenericKey key);

    /**
     * Builds the package
     *
     * @return The built package or empty if the package is empty
     */
    ItemStack build();
}
