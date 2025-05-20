package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public interface IngredientIdentifiedInventory {
    @Nullable InventoryIdentifier identifier();

    BlockCapability<?, ?> capability();

    Object handler();

    <T, C> void setCapability(BlockCapability<T, C> capability, T handler);

    static IngredientIdentifiedInventory from(IdentifiedInventory identifiedInventory) {
        return (IngredientIdentifiedInventory) (Object) identifiedInventory;
    }
}
