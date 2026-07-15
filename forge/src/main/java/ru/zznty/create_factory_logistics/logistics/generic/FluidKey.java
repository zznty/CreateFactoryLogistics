package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.ConcreteGenericKey;

public final class FluidKey extends ConcreteGenericKey<Fluid> {

    public FluidKey(Holder<Fluid> fluid, @Nullable PatchedDataComponentMap nbt) {
        super(fluid, nbt);
    }

    public FluidStack stack() {
        return new FluidStack(holder, 1, nbt.asPatch());
    }
}
