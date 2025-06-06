package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

public record FluidKey(Fluid fluid, @Nullable CompoundTag nbt) implements GenericKey {
    public FluidStack stack() {
        return new FluidStack(fluid, 1, nbt);
    }
}
