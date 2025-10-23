package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.foundation.fluid.FluidHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.List;

final class NetworkFluidHandler implements IFluidHandler {
    private final List<GenericStack> stacks;

    public NetworkFluidHandler(GenericInventorySummaryProvider summaryProvider) {
        GenericInventorySummary summary = GenericInventorySummary.empty();
        summaryProvider.apply(summary);
        stacks = summary.get().stream().filter(s -> s.key() instanceof FluidKey).toList();
    }

    @Override
    public int getTanks() {
        return stacks.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return asFluid(stacks.get(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE - 1 / getTanks();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(getFluidInTank(tank), stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (action != FluidAction.SIMULATE || resource.isEmpty())
            return FluidStack.EMPTY;

        int amount = 0;
        for (GenericStack stack : stacks) {
            FluidStack fluidStack = asFluid(stack);
            if (FluidStack.isSameFluidSameComponents(fluidStack, resource))
                amount += Math.min(fluidStack.getAmount(), resource.getAmount() - amount);
            if (amount >= resource.getAmount()) break;
        }

        return FluidHelper.copyStackWithAmount(resource, amount);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack resource = FluidStack.EMPTY;
        for (GenericStack stack : stacks) {
            FluidStack fluidStack = asFluid(stack);
            if (!fluidStack.isEmpty()) {
                resource = fluidStack;
                break;
            }
        }

        return drain(FluidHelper.copyStackWithAmount(resource, maxDrain), action);
    }

    private static FluidStack asFluid(GenericStack stack) {
        if (stack.key() instanceof FluidKey fluidKey)
            return FluidHelper.copyStackWithAmount(fluidKey.stack(), stack.amount());
        return FluidStack.EMPTY;
    }
}
