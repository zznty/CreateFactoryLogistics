package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public abstract class GenericFilterItemStack extends FilterItemStack {
    protected GenericFilterItemStack(ItemStack filter) {
        super(filter);
    }

    public boolean test(Level world, GenericStack stack) {
        return test(world, stack, true);
    }

    public abstract boolean test(Level world, GenericStack stack, boolean matchNBT);

    @Override
    public boolean test(Level world, ItemStack stack, boolean matchNBT) {
        if (isEmpty()) return true;
        return test(world, GenericStack.wrap(stack), matchNBT);
    }

    @Override
    public boolean test(Level world, FluidStack stack, boolean matchNBT) {
        if (isEmpty()) return true;
        return false;
    }
}
