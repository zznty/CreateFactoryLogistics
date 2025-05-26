package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public final class FluidGenericStack {
    public static GenericStack wrap(FluidStack stack) {
        if (stack.getRawFluid() == Fluids.EMPTY) return GenericStack.EMPTY;
        return new GenericStack(new FluidKey(stack.getRawFluid(), stack.getTag()), stack.getAmount());
    }
}
