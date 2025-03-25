package ru.zznty.create_factory_logistics.logistics.jar;

import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageVisual;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import ru.zznty.create_factory_logistics.render.FluidVisual;

public class JarVisual extends PackageVisual {

    private final FluidVisual fluid;
    private final JarPackageEntity jarEntity;
    private final FluidStack fluidStack;

    public JarVisual(VisualizationContext ctx, PackageEntity entity, float partialTick) {
        super(ctx, entity, partialTick);

        jarEntity = (JarPackageEntity) entity;
        fluidStack = FluidUtil.getFluidContained(entity.getBox()).orElse(FluidStack.EMPTY);
        fluid = new FluidVisual(ctx, false, true);
    }

    @Override
    public void beginFrame(Context ctx) {
        super.beginFrame(ctx);

        fluid.begin();
        
        FluidStack fluidStack = FluidHelper.copyStackWithAmount(this.fluidStack, (int) jarEntity.fluidLevel.getValue());

        TransformedInstance[] buffers = fluid.setupBuffers(fluidStack, 0);

        for (int i = 0; i < buffers.length; i++) {
            fluid.setupBuffer(fluidStack, JarPackageItem.JAR_CAPACITY, buffers[i], i, 8f / 16, 8f / 16);
        }

        fluid.end();
    }

    @Override
    protected void _delete() {
        super._delete();
        fluid.delete();
    }
}
