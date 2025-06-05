package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;

public interface GenericIdentifiedInventory {
    @Nullable InventoryIdentifier identifier();

    BlockCapability<?, ?> capability();

    Object handler();

    <T> void setCapability(BlockCapability<T, ?> capability, T handler);

    static GenericIdentifiedInventory from(IdentifiedInventory identifiedInventory) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return (GenericIdentifiedInventory) (Object) identifiedInventory;

        return new GenericIdentifiedInventory() {
            private final IdentifiedInventory inventory = identifiedInventory;

            @Override
            public @Nullable InventoryIdentifier identifier() {
                return inventory.identifier();
            }

            @Override
            public BlockCapability<?, ?> capability() {
                return Capabilities.ItemHandler.BLOCK;
            }

            @Override
            public Object handler() {
                return inventory.handler();
            }

            @Override
            public <T> void setCapability(BlockCapability<T, ?> capability, T handler) {
            }
        };
    }
}
