package ru.zznty.create_factory_logistics.mixin.logistics.filter;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.zznty.create_factory_abstractions.generic.support.GenericFilterItemStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;

@Mixin(GenericFilterItemStack.class)
public class GenericFilterItemStackMixin extends FilterItemStack {
    protected GenericFilterItemStackMixin(ItemStack filter) {
        super(filter);
    }

    @Overwrite(remap = false)
    public boolean test(Level world, FluidStack stack, boolean matchNBT) {
        if (isEmpty()) return true;
        //noinspection DataFlowIssue
        return ((GenericFilterItemStack) (Object) this).test(world, FluidGenericStack.wrap(stack), matchNBT);
    }
}
