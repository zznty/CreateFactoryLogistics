package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

public record FluidKey(Holder<Fluid> fluid, PatchedDataComponentMap nbt) implements GenericKey {
    public FluidStack stack() {
        return new FluidStack(fluid, 1, nbt.asPatch());
    }
}
