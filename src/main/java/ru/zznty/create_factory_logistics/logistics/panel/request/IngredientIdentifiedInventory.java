package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

public interface IngredientIdentifiedInventory {
    @Nullable InventoryIdentifier identifier();

    Capability<?> capability();

    Object handler();

    <T> void setCapability(Capability<T> capability, T handler);

    static IngredientIdentifiedInventory from(IdentifiedInventory identifiedInventory) {
        return (IngredientIdentifiedInventory) (Object) identifiedInventory;
    }
}
