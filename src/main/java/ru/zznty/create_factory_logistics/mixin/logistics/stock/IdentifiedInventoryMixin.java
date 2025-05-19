package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientIdentifiedInventory;

@Mixin(IdentifiedInventory.class)
public class IdentifiedInventoryMixin implements IngredientIdentifiedInventory {
    @Final
    @Shadow(remap = false)
    @Nullable
    private IItemHandler handler;

    @Final
    @Shadow(remap = false)
    @Nullable
    private InventoryIdentifier identifier;

    @Unique
    @Nullable
    private Capability<?> createFactoryLogistics$capability;

    @Unique
    @Nullable
    private Object createFactoryLogistics$handler;

    @Override
    public @Nullable InventoryIdentifier identifier() {
        return identifier;
    }

    @Override
    public Capability<?> capability() {
        return handler != null ? ForgeCapabilities.ITEM_HANDLER : createFactoryLogistics$capability;
    }

    @Override
    public Object handler() {
        return handler != null ? handler : createFactoryLogistics$handler;
    }

    @Override
    public <T> void setCapability(Capability<T> capability, T handler) {
        if (this.handler != null)
            throw new IllegalArgumentException("Handler must be null");
        createFactoryLogistics$capability = capability;
        createFactoryLogistics$handler = handler;
    }
}
