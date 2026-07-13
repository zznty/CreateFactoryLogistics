package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityChemicalTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlocks;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackagerBlockEntity;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBehaviour;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBlockEntity;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlockEntity;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlockEntity;

import java.util.UUID;

final class FactoryPanelRecipeFixture {
    static ItemNetwork placeItemNetwork(GameTestHelper helper, int x, UUID frequency,
                                        ItemStack contents) {
        BlockPos storagePos = new BlockPos(x, 1, 0);
        BlockPos packagerPos = new BlockPos(x, 1, 1);
        BlockPos linkPos = new BlockPos(x, 1, 2);
        helper.setBlock(storagePos, Blocks.BARREL);
        helper.setBlock(packagerPos, packagerState(AllBlocks.PACKAGER.get()));
        helper.setBlock(linkPos, stockLinkState());
        Container storage = helper.getBlockEntity(storagePos);
        if (!contents.isEmpty()) {
            storage.setItem(0, contents.copy());
            storage.setChanged();
        }
        PackagerBlockEntity packager = helper.getBlockEntity(packagerPos);
        PackagerLinkBlockEntity link = helper.getBlockEntity(linkPos);
        link.behaviour.freqId = frequency;
        return new ItemNetwork(storage, packager, link);
    }

    static FluidNetwork placeFluidNetwork(GameTestHelper helper, int x, UUID frequency,
                                          FluidStack contents) {
        BlockPos tankPos = new BlockPos(x, 1, 0);
        BlockPos packagerPos = new BlockPos(x, 1, 1);
        BlockPos linkPos = new BlockPos(x, 1, 2);
        helper.setBlock(tankPos, AllBlocks.FLUID_TANK.getDefaultState());
        helper.setBlock(packagerPos, packagerState(FactoryBlocks.JAR_PACKAGER.get()));
        helper.setBlock(linkPos, stockLinkState());
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(tankPos), null);
        helper.assertTrue(tank != null, "fluid tank has no capability");
        helper.assertValueEqual(tank.fill(contents.copy(), IFluidHandler.FluidAction.EXECUTE),
                contents.getAmount(), "fluid tank fill");
        JarPackagerBlockEntity packager = helper.getBlockEntity(packagerPos);
        packager.drainInventory.findNewCapability();
        PackagerLinkBlockEntity link = helper.getBlockEntity(linkPos);
        link.behaviour.freqId = frequency;
        return new FluidNetwork(tank, packager, link);
    }

    static ChemicalNetwork placeChemicalNetwork(GameTestHelper helper, int x, UUID frequency,
                                                  ChemicalStack contents) {
        BlockPos tankPos = new BlockPos(x, 1, 0);
        BlockPos packagerPos = new BlockPos(x, 1, 1);
        BlockPos linkPos = new BlockPos(x, 1, 2);
        helper.setBlock(tankPos, MekanismBlocks.BASIC_CHEMICAL_TANK.defaultState());
        helper.setBlock(packagerPos, packagerState(FactoryMekanismBlocks.BARREL_PACKAGER.get()));
        helper.setBlock(linkPos, stockLinkState());
        TileEntityChemicalTank tank = helper.getBlockEntity(tankPos);
        tank.getChemicalTank().setStack(contents.copy());
        tank.setChanged();
        BarrelPackagerBlockEntity packager = helper.getBlockEntity(packagerPos);
        packager.drainInventory.bypassSidedness();
        packager.drainInventory.findNewCapability();
        PackagerLinkBlockEntity link = helper.getBlockEntity(linkPos);
        link.behaviour.freqId = frequency;
        return new ChemicalNetwork(tank, packager, link);
    }

    static ItemPanel placeItemPanel(GameTestHelper helper, BlockPos pos, UUID frequency,
                                    ItemStack filter, int count) {
        helper.setBlock(pos.below(), Blocks.COPPER_BLOCK);
        helper.setBlock(pos, panelState(AllBlocks.FACTORY_GAUGE.get()));
        FactoryPanelBlockEntity be = helper.getBlockEntity(pos);
        helper.assertTrue(be.addPanel(FactoryPanelBlock.PanelSlot.BOTTOM_LEFT, frequency),
                "failed to activate item panel");
        FactoryPanelBehaviour panel = be.panels.get(FactoryPanelBlock.PanelSlot.BOTTOM_LEFT);
        helper.assertTrue(panel.setFilter(filter.copyWithCount(1)), "item filter was rejected");
        panel.count = count;
        panel.upTo = true;
        panel.redstonePowered = true;
        return new ItemPanel(panel, frequency);
    }

    static FluidPanel placeFluidPanel(GameTestHelper helper, BlockPos pos, UUID frequency,
                                      ItemStack filter, int amount) {
        helper.setBlock(pos.below(), Blocks.COPPER_BLOCK);
        helper.setBlock(pos, panelState(FactoryBlocks.FACTORY_FLUID_GAUGE.get()));
        FactoryFluidPanelBlockEntity be = helper.getBlockEntity(pos);
        helper.assertTrue(be.addPanel(FactoryPanelBlock.PanelSlot.BOTTOM_LEFT, frequency),
                "failed to activate fluid panel");
        FactoryFluidPanelBehaviour panel =
                (FactoryFluidPanelBehaviour) be.panels.get(FactoryPanelBlock.PanelSlot.BOTTOM_LEFT);
        helper.assertTrue(panel.setFilter(filter.copyWithCount(1)), "fluid filter was rejected");
        panel.count = amount;
        panel.redstonePowered = true;
        return new FluidPanel(panel, frequency);
    }

    static ChemicalPanel placeChemicalPanel(GameTestHelper helper, BlockPos pos, UUID frequency,
                                            ItemStack filter, int amount) {
        helper.setBlock(pos.below(), Blocks.COPPER_BLOCK);
        helper.setBlock(pos, panelState(FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE.get()));
        FactoryChemicalPanelBlockEntity be = helper.getBlockEntity(pos);
        helper.assertTrue(be.addPanel(FactoryPanelBlock.PanelSlot.BOTTOM_LEFT, frequency),
                "failed to activate chemical panel");
        FactoryChemicalPanelBehaviour panel =
                (FactoryChemicalPanelBehaviour) be.panels.get(FactoryPanelBlock.PanelSlot.BOTTOM_LEFT);
        helper.assertTrue(panel.setFilter(filter.copyWithCount(1)), "chemical filter was rejected");
        panel.count = amount;
        panel.redstonePowered = true;
        return new ChemicalPanel(panel, frequency);
    }

    static void connect(FactoryPanelBehaviour source, FactoryPanelBehaviour target, int amount) {
        target.addConnection(source.getPanelPosition());
        target.targetedBy.get(source.getPanelPosition()).amount = amount;
    }

    static void activateRecipe(FactoryPanelBehaviour target, String address, int output) {
        target.recipeAddress = address;
        target.recipeOutput = output;
        target.redstonePowered = false;
        target.tick();
    }

    private static BlockState stockLinkState() {
        return AllBlocks.STOCK_LINK.getDefaultState()
                .setValue(PackagerLinkBlock.FACE, AttachFace.WALL)
                .setValue(PackagerLinkBlock.FACING, Direction.SOUTH)
                .setValue(PackagerLinkBlock.POWERED, false)
                .setValue(PackagerLinkBlock.WATERLOGGED, false);
    }

    private static BlockState packagerState(net.minecraft.world.level.block.Block block) {
        return block.defaultBlockState()
                .setValue(PackagerBlock.FACING, Direction.SOUTH)
                .setValue(PackagerBlock.POWERED, false)
                .setValue(PackagerBlock.LINKED, false);
    }

    private static BlockState panelState(net.minecraft.world.level.block.Block block) {
        return block.defaultBlockState()
                .setValue(FactoryPanelBlock.FACE, AttachFace.FLOOR)
                .setValue(FactoryPanelBlock.FACING, Direction.NORTH)
                .setValue(FactoryPanelBlock.WATERLOGGED, false)
                .setValue(FactoryPanelBlock.POWERED, false);
    }

    record ItemNetwork(Container storage, PackagerBlockEntity packager,
                       PackagerLinkBlockEntity link) {}
    record FluidNetwork(IFluidHandler tank, JarPackagerBlockEntity packager,
                        PackagerLinkBlockEntity link) {}
    record ChemicalNetwork(TileEntityChemicalTank tank, BarrelPackagerBlockEntity packager,
                           PackagerLinkBlockEntity link) {}
    record ItemPanel(FactoryPanelBehaviour behaviour, UUID frequency) {}
    record FluidPanel(FactoryFluidPanelBehaviour behaviour, UUID frequency) {}
    record ChemicalPanel(FactoryChemicalPanelBehaviour behaviour, UUID frequency) {}

    private FactoryPanelRecipeFixture() {}
}
