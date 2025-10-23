package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericKeyProviderExtension;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;

import java.util.Optional;

public class FluidGenericExtension implements GenericKeyProviderExtension<FluidKey, FluidStack, Fluid, IFluidHandler> {
    private final GenericCapabilityWrapperProvider<IFluidHandler> provider = new GenericCapabilityWrapperProvider<>() {
        @Override
        public Capability<IFluidHandler> capability() {
            return ForgeCapabilities.FLUID_HANDLER;
        }

        @Override
        public IFluidHandler wrap(GenericInventorySummaryProvider summaryProvider) {
            return new NetworkFluidHandler(summaryProvider);
        }

        @Override
        public GenericInventorySummaryProvider unwrap(IFluidHandler capability) {
            return new FluidInventorySummaryProvider(capability);
        }
    };

    @Override
    public FluidKey defaultKey() {
        return wrap(FluidStack.EMPTY);
    }

    @Override
    public FluidKey wrap(FluidStack fluidStack) {
        return new FluidKey(fluidStack.getRawFluid(), fluidStack.getTag());
    }

    @Override
    public FluidKey wrapGeneric(FluidStack fluidStack) {
        return new FluidKey(fluidStack.getRawFluid(), null);
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
        return ForgeRegistries.FLUIDS.getResourceKey(key.fluid());
    }

    @Override
    public GenericCapabilityWrapperProvider<IFluidHandler> capabilityWrapperProvider() {
        return provider;
    }

    @Override
    public int compare(FluidKey a, FluidKey b) {
        ResourceLocation akey = ForgeRegistries.FLUIDS.getKey(a.fluid());
        if (akey == null) return -1;
        ResourceLocation bKey = ForgeRegistries.FLUIDS.getKey(b.fluid());
        if (bKey == null) return 1;
        return akey.compareTo(bKey);
    }
}
