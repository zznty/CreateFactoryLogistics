package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

public record FluidInventorySummaryProvider(IFluidHandler handler) implements GenericInventorySummaryProvider {
    @Override
    public void apply(GenericInventorySummary summary) {
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack stack = handler.getFluidInTank(i);
            if (!stack.isEmpty()) {
                if (handler instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
                    stack.setAmount(BigItemStack.INF);

                summary.add(FluidGenericStack.wrap(stack));
            }
        }
    }
}
