package ru.zznty.create_factory_logistics.mixin.accessor;

import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HosePulleyFluidHandler.class)
public interface HosePulleyFluidHandlerAccessor {
    @Accessor
    FluidDrainingBehaviour getDrainer();
}
