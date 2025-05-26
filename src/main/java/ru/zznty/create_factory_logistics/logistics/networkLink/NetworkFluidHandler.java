package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.foundation.fluid.FluidHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;

import java.util.UUID;

public class NetworkFluidHandler extends BaseNetworkHandler implements IFluidHandler {
    public NetworkFluidHandler(UUID network, NetworkLinkMode mode) {
        super(network, mode);
    }

    @Override
    public int getTanks() {
        return summary().size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return asFluid(summary().get(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE - 1 / getTanks();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(asFluid(summary().get(tank)), stack);
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
        for (GenericStack stack : summary()) {
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
        for (GenericStack stack : summary()) {
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
