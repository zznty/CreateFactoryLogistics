package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.mixin.accessor.HosePulleyFluidHandlerAccessor;

public record FluidInventorySummaryProvider(IFluidHandler handler) implements GenericInventorySummaryProvider {
    @Override
    public void apply(boolean scanInputSlots, GenericInventorySummary summary) {
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack stack = handler.getFluidInTank(i);
            if (!stack.isEmpty()) {
                if (!scanInputSlots)
                    stack = handler.drain(stack, IFluidHandler.FluidAction.SIMULATE);
                if (handler instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank ||
                        (handler instanceof HosePulleyFluidHandlerAccessor hosePulleyHandler && hosePulleyHandler.getDrainer().isInfinite()))
                    stack.setAmount(BigItemStack.INF);

                summary.add(FluidGenericStack.wrap(stack));
            }
        }
    }
}
