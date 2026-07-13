package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlockEntity;

import java.util.UUID;

final class FactoryPanelTestSupport {
    static final FactoryPanelBlock.PanelSlot SLOT = FactoryPanelBlock.PanelSlot.BOTTOM_LEFT;

    static PanelFixture placePanel(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position.below(), Blocks.COPPER_BLOCK);
        BlockState state = FactoryBlocks.FACTORY_FLUID_GAUGE.getDefaultState()
                .setValue(FactoryPanelBlock.FACE, AttachFace.FLOOR)
                .setValue(FactoryPanelBlock.FACING, Direction.NORTH)
                .setValue(FactoryPanelBlock.WATERLOGGED, false)
                .setValue(FactoryPanelBlock.POWERED, false);
        helper.setBlock(position, state);

        FactoryFluidPanelBlockEntity blockEntity = helper.getBlockEntity(position);
        UUID network = UUID.randomUUID();
        helper.assertTrue(blockEntity.addPanel(SLOT, network), "failed to activate panel");
        FactoryFluidPanelBehaviour behaviour =
                (FactoryFluidPanelBehaviour) blockEntity.panels.get(SLOT);
        return new PanelFixture(position, blockEntity, behaviour, network);
    }

    static PanelFixture placeWaterPanel(GameTestHelper helper, BlockPos position, int count) {
        PanelFixture fixture = placePanel(helper, position);
        helper.assertTrue(fixture.behaviour().setFilter(Items.WATER_BUCKET.getDefaultInstance()),
                "water filter was rejected");
        fixture.behaviour().count = count;
        fixture.behaviour().upTo = true;
        return fixture;
    }

    static FactoryPanelConnection connect(PanelFixture source, PanelFixture target, int amount) {
        FactoryPanelConnection connection = new FactoryPanelConnection(
                source.behaviour().getPanelPosition(), amount);
        target.behaviour().targetedBy.put(source.behaviour().getPanelPosition(), connection);
        source.behaviour().targeting.add(target.behaviour().getPanelPosition());
        return connection;
    }

    record PanelFixture(BlockPos position, FactoryFluidPanelBlockEntity blockEntity,
                        FactoryFluidPanelBehaviour behaviour, UUID network) {
    }

    private FactoryPanelTestSupport() {
    }
}
