package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.UUID;

final class LogisticsTestFixture {
    static final BlockPos STORAGE = new BlockPos(0, 1, 0);
    static final BlockPos PACKAGER = new BlockPos(0, 1, 1);
    static final BlockPos LINK = new BlockPos(0, 1, 2);

    static ItemNetwork placeItemNetwork(GameTestHelper helper, UUID frequency, ItemStack contents) {
        helper.setBlock(STORAGE, Blocks.BARREL);
        helper.setBlock(PACKAGER, AllBlocks.PACKAGER.getDefaultState()
                .setValue(PackagerBlock.FACING, Direction.SOUTH));
        helper.setBlock(LINK, AllBlocks.STOCK_LINK.getDefaultState()
                .setValue(PackagerLinkBlock.FACE, AttachFace.WALL)
                .setValue(PackagerLinkBlock.FACING, Direction.SOUTH));

        Container storage = helper.getBlockEntity(STORAGE);
        storage.setItem(0, contents.copy());
        storage.setChanged();
        PackagerBlockEntity packager = helper.getBlockEntity(PACKAGER);
        PackagerLinkBlockEntity link = helper.getBlockEntity(LINK);
        link.behaviour.freqId = frequency;
        return new ItemNetwork(storage, packager, link);
    }

    record ItemNetwork(Container storage, PackagerBlockEntity packager,
                       PackagerLinkBlockEntity link) {
    }

    private LogisticsTestFixture() {
    }
}
