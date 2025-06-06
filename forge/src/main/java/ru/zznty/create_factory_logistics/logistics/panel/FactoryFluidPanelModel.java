package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelModel;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import ru.zznty.create_factory_logistics.FactoryModels;

import java.util.Arrays;
import java.util.List;

public class FactoryFluidPanelModel extends FactoryPanelModel {
    public FactoryFluidPanelModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public void addPanel(List<BakedQuad> quads, BlockState state, FactoryPanelBlock.PanelSlot slot, FactoryPanelBlock.PanelType type, FactoryPanelBlock.PanelState panelState, RandomSource rand, ModelData data, RenderType renderType, boolean ponder) {
        PartialModel factoryPanel = panelState == FactoryPanelBlock.PanelState.PASSIVE
                ? type == FactoryPanelBlock.PanelType.NETWORK ? FactoryModels.FACTORY_FLUID_PANEL : FactoryModels.FACTORY_FLUID_PANEL_RESTOCKER
                : type == FactoryPanelBlock.PanelType.NETWORK ? FactoryModels.FACTORY_FLUID_PANEL_WITH_BULB
                : FactoryModels.FACTORY_FLUID_PANEL_RESTOCKER_WITH_BULB;

        List<BakedQuad> quadsToAdd = factoryPanel.get()
                .getQuads(state, null, rand, data, RenderType.solid());

        float xRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getXRot(state);
        float yRot = Mth.RAD_TO_DEG * FactoryPanelBlock.getYRot(state);

        for (BakedQuad bakedQuad : quadsToAdd) {
            int[] vertices = bakedQuad.getVertices();
            int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            Vec3 quadNormal = Vec3.atLowerCornerOf(bakedQuad.getDirection()
                    .getNormal());
            quadNormal = VecHelper.rotate(quadNormal, 180, Direction.Axis.Y);
            quadNormal = VecHelper.rotate(quadNormal, xRot + 90, Direction.Axis.X);
            quadNormal = VecHelper.rotate(quadNormal, yRot, Direction.Axis.Y);

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
                Vec3 normal = BakedQuadHelper.getNormalXYZ(vertices, i);

                vertex = vertex.add(slot.xOffset * .5, 0, slot.yOffset * .5);
                vertex = VecHelper.rotateCentered(vertex, 180, Direction.Axis.Y);
                vertex = VecHelper.rotateCentered(vertex, xRot + 90, Direction.Axis.X);
                vertex = VecHelper.rotateCentered(vertex, yRot, Direction.Axis.Y);

                normal = VecHelper.rotate(normal, 180, Direction.Axis.Y);
                normal = VecHelper.rotate(normal, xRot + 90, Direction.Axis.X);
                normal = VecHelper.rotate(normal, yRot, Direction.Axis.Y);

                BakedQuadHelper.setXYZ(transformedVertices, i, vertex);
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3(0, 1, 0));
            }

            Direction newNormal = Direction.fromDelta((int) Math.round(quadNormal.x), (int) Math.round(quadNormal.y),
                    (int) Math.round(quadNormal.z));
            quads.add(new BakedQuad(transformedVertices, bakedQuad.getTintIndex(), newNormal, bakedQuad.getSprite(),
                    !ponder && bakedQuad.isShade()));
        }
    }
}
