package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericKeyProviderExtension;

public class FluidGenericExtension implements GenericKeyProviderExtension<FluidKey, FluidStack> {
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
    public int compare(FluidKey a, FluidKey b) {
        ResourceLocation akey = ForgeRegistries.FLUIDS.getKey(a.fluid());
        if (akey == null) return -1;
        ResourceLocation bKey = ForgeRegistries.FLUIDS.getKey(b.fluid());
        if (bKey == null) return 1;
        return akey.compareTo(bKey);
    }
}
