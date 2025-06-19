package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericKeyProviderExtension;

import java.util.Optional;

public class FluidGenericExtension implements GenericKeyProviderExtension<FluidKey, FluidStack, Fluid> {
    @Override
    public FluidKey defaultKey() {
        return wrap(FluidStack.EMPTY);
    }

    @Override
    public FluidKey wrap(FluidStack fluidStack) {
        return new FluidKey(fluidStack.getFluidHolder(), fluidStack.getComponents());
    }

    @Override
    public FluidKey wrapGeneric(FluidStack fluidStack) {
        return new FluidKey(fluidStack.getFluidHolder(), new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    @Override
    public FluidStack unwrap(FluidKey key) {
        return key.stack();
    }

    @Override
    public String ingredientTypeUid() {
        return "fluid_stack";
    }

    @Override
    public Optional<ResourceKey<Fluid>> resourceKey(FluidKey key) {
        return Optional.ofNullable(key.fluid().getKey());
    }

    @Override
    public int compare(FluidKey a, FluidKey b) {
        @Nullable ResourceKey<Fluid> akey = a.fluid().getKey();
        if (akey == null) return -1;
        @Nullable ResourceKey<Fluid> bKey = b.fluid().getKey();
        if (bKey == null) return 1;
        return akey.compareTo(bKey);
    }
}
