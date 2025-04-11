package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientCasts;

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
        return IngredientCasts.asFluidStack(summary().get(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE - 1 / getTanks();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return IngredientCasts.asFluidStack(summary().get(tank)).isFluidEqual(stack);
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
        for (BoardIngredient ingredient : summary()) {
            FluidStack stack = IngredientCasts.asFluidStack(ingredient);
            if (stack.isFluidEqual(resource))
                amount += Math.min(stack.getAmount(), resource.getAmount() - amount);
            if (amount >= resource.getAmount()) break;
        }

        return FluidHelper.copyStackWithAmount(resource, amount);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack resource = FluidStack.EMPTY;
        for (BoardIngredient ingredient : summary()) {
            FluidStack stack = IngredientCasts.asFluidStack(ingredient);
            if (!stack.isEmpty()) {
                resource = stack;
                break;
            }
        }

        return drain(FluidHelper.copyStackWithAmount(resource, maxDrain), action);
    }
}
