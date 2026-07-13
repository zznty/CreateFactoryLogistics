package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.FactoryItems;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlockEntity;

import java.util.List;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class CompositeArmGameTests {
    private static final BlockPos DEPOT = new BlockPos(3, 1, 1);
    private static final BlockPos ARM = new BlockPos(3, 1, 3);
    private static final BlockPos MOTOR = new BlockPos(3, 0, 3);
    private static final BlockPos ITEM_PACKAGER = new BlockPos(1, 1, 3);
    private static final BlockPos ITEM_STORAGE = new BlockPos(1, 1, 4);
    private static final BlockPos JAR_PACKAGER = new BlockPos(5, 1, 3);
    private static final BlockPos FLUID_STORAGE = new BlockPos(5, 1, 4);

    @GameTest(template = "empty", batch = "arm_composite_e2e", timeoutTicks = 300)
    public static void armDecomposesCompositeIntoPackagerAndBottler(GameTestHelper helper) {
        placeFixture(helper);
        DepotBlockEntity depot = helper.getBlockEntity(DEPOT);
        ArmBlockEntity arm = helper.getBlockEntity(ARM);
        PackagerBlockEntity itemPackager = helper.getBlockEntity(ITEM_PACKAGER);
        JarPackagerBlockEntity jarPackager = helper.getBlockEntity(JAR_PACKAGER);
        Container itemStorage = helper.getBlockEntity(ITEM_STORAGE);
        IFluidHandler fluidStorage = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(FLUID_STORAGE), null);
        helper.assertTrue(fluidStorage != null, "fluid tank has no capability");
        programArm(helper, arm);
        CreativeMotorBlockEntity motor = helper.getBlockEntity(MOTOR);
        motor.generatedSpeed.setValue(256);
        jarPackager.drainInventory.findNewCapability();

        ItemStackHandler directContents = new ItemStackHandler(PackageItem.SLOTS);
        directContents.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));
        directContents.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT, 3));
        ItemStack composite = CompositePackageItem.of(helper.getLevel().registryAccess(),
                PackageItem.containing(directContents), List.of(waterJar(1000)));

        helper.runAfterDelay(3, () -> {
            helper.assertTrue(Math.abs(arm.getSpeed()) > 0, "arm has no kinetic speed");
            CompoundTag configured = arm.saveWithFullMetadata(helper.getLevel().registryAccess());
            helper.assertValueEqual(configured.getList("InteractionPoints", CompoundTag.TAG_COMPOUND).size(),
                    3, "arm interaction point count");
            depot.setHeldItem(composite);
            helper.succeedWhen(() -> {
                helper.assertTrue(depot.getHeldItem().isEmpty(), "depot still holds composite");
                helper.assertValueEqual(count(itemStorage, Items.IRON_INGOT), 12, "unpacked iron");
                helper.assertValueEqual(count(itemStorage, Items.GOLD_INGOT), 3, "unpacked gold");
                FluidStack fluid = fluidStorage.getFluidInTank(0);
                helper.assertTrue(fluid.is(Fluids.WATER), "tank contains wrong fluid");
                helper.assertValueEqual(fluid.getAmount(), 1000, "unpacked water");
                helper.assertTrue(heldByArm(helper, arm).isEmpty(), "arm still holds package remainder");
                helper.assertTrue(itemPackager.previouslyUnwrapped.getItem() instanceof PackageItem,
                        "ordinary packager did not unwrap package");
                helper.assertTrue(jarPackager.previouslyUnwrapped.is(FactoryItems.REGULAR_JAR.get()),
                        "bottler did not unwrap jar");
            });
        });
    }

    private static void placeFixture(GameTestHelper helper) {
        helper.setBlock(DEPOT, AllBlocks.DEPOT.getDefaultState());
        helper.setBlock(MOTOR, AllBlocks.CREATIVE_MOTOR.getDefaultState()
                .setValue(DirectionalBlock.FACING, Direction.UP));
        helper.setBlock(ARM, AllBlocks.MECHANICAL_ARM.getDefaultState()
                .setValue(ArmBlock.CEILING, false));
        helper.setBlock(ITEM_PACKAGER, AllBlocks.PACKAGER.getDefaultState()
                .setValue(PackagerBlock.FACING, Direction.NORTH)
                .setValue(PackagerBlock.POWERED, false)
                .setValue(PackagerBlock.LINKED, false));
        helper.setBlock(ITEM_STORAGE, Blocks.BARREL);
        helper.setBlock(JAR_PACKAGER, FactoryBlocks.JAR_PACKAGER.getDefaultState()
                .setValue(PackagerBlock.FACING, Direction.NORTH)
                .setValue(PackagerBlock.POWERED, false)
                .setValue(PackagerBlock.LINKED, false));
        helper.setBlock(FLUID_STORAGE, AllBlocks.FLUID_TANK.getDefaultState());
    }

    private static void programArm(GameTestHelper helper, ArmBlockEntity arm) {
        BlockPos armPos = helper.absolutePos(ARM);
        ListTag points = new ListTag();
        ArmInteractionPoint input = point(helper, DEPOT);
        input.cycleMode();
        points.add(input.serialize(armPos));
        points.add(point(helper, ITEM_PACKAGER).serialize(armPos));
        points.add(point(helper, JAR_PACKAGER).serialize(armPos));
        CompoundTag tag = arm.saveWithFullMetadata(helper.getLevel().registryAccess());
        tag.put("InteractionPoints", points);
        arm.loadWithComponents(tag, helper.getLevel().registryAccess());
    }

    private static ArmInteractionPoint point(GameTestHelper helper, BlockPos relative) {
        BlockPos absolute = helper.absolutePos(relative);
        ArmInteractionPoint point = ArmInteractionPoint.create(helper.getLevel(), absolute,
                helper.getBlockState(relative));
        helper.assertTrue(point != null, "missing arm interaction point at " + relative);
        return point;
    }

    private static ItemStack heldByArm(GameTestHelper helper, ArmBlockEntity arm) {
        CompoundTag tag = arm.saveWithFullMetadata(helper.getLevel().registryAccess());
        return ItemStack.parseOptional(helper.getLevel().registryAccess(), tag.getCompound("HeldItem"));
    }

    private static ItemStack waterJar(int amount) {
        ItemStack jar = FactoryItems.REGULAR_JAR.asStack();
        var handler = jar.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null || handler.fill(new FluidStack(Fluids.WATER, amount),
                IFluidHandler.FluidAction.EXECUTE) != amount)
            throw new IllegalStateException("could not fill test jar");
        return jar;
    }

    private static int count(Container container, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++)
            if (container.getItem(slot).is(item))
                total += container.getItem(slot).getCount();
        return total;
    }

    private CompositeArmGameTests() {}
}
