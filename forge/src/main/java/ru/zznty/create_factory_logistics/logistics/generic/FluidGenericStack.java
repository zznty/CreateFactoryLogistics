package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public final class FluidGenericStack {
    public static GenericStack wrap(FluidStack stack) {
        if (stack.getFluid() == Fluids.EMPTY) return GenericStack.EMPTY;
        return new GenericStack(new FluidKey(stack.getFluidHolder(), stack.getComponents()), stack.getAmount());
    }
}
