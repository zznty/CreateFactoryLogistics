package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.content.logistics.packager.PackagerBlock;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class JarPackagerVisual<T extends JarPackagerBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
    public final TransformedInstance tray;
    public float lastTrayOffset = Float.NaN;

    public JarPackagerVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        tray = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(JarPackagerRenderer.getTrayModel(blockState)))
                .createInstance();

        animate(partialTick);
    }

    @Override
    public void beginFrame(Context ctx) {
        animate(ctx.partialTick());
    }

    public void animate(float partialTick) {
        float trayOffset = blockEntity.getTrayOffset(partialTick) / 2;

        if (trayOffset != lastTrayOffset) {
            Direction facing = blockState.getValue(PackagerBlock.FACING)
                    .getOpposite();

            if (facing.getAxis() == Direction.Axis.Y)
                facing = Direction.NORTH;

            var lowerCorner = Vec3.atLowerCornerOf(Direction.DOWN.getNormal());

            tray.setIdentityTransform()
                    .translate(getVisualPosition())
                    .translate(lowerCorner.scale(trayOffset))
                    .rotateCentered(facing.getRotation())
                    .rotateXCenteredDegrees(90)
                    .setChanged();

            lastTrayOffset = trayOffset;
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(tray);
    }

    @Override
    protected void _delete() {
        tray.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {

    }
}
