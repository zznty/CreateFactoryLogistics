package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

import java.util.List;
import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class RedstoneRequesterGameTests {
    private static final BlockPos REQUESTER = new BlockPos(6, 1, 4);

    @GameTest(template = "empty", batch = "redstone_requester_e2e", timeoutTicks = 80)
    public static void fullAndPartialItemRequestsUseRealPackager(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        var network = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, frequency,
                new ItemStack(Items.DIAMOND, 9));
        RedstoneRequesterBlockEntity requester = placeRequester(helper, frequency,
                GenericOrder.order(List.of(GenericStack.wrap(new ItemStack(Items.DIAMOND, 5)))),
                "warehouse", false);

        helper.runAfterDelay(3, () -> {
            requester.triggerRequest();
            helper.assertValueEqual(network.storage().getItem(0).getCount(), 4,
                    "remaining diamonds");
            helper.assertTrue(requester.lastRequestSucceeded, "full request did not succeed");
            assertItemPackage(helper, network.packager().heldBox, Items.DIAMOND, 5, "warehouse");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "redstone_requester_e2e", timeoutTicks = 80)
    public static void partialDisabledDoesNotExtract(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        var network = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, frequency,
                new ItemStack(Items.DIAMOND, 5));
        RedstoneRequesterBlockEntity requester = placeRequester(helper, frequency,
                GenericOrder.order(List.of(GenericStack.wrap(new ItemStack(Items.DIAMOND, 8)))),
                "no_partial", false);

        helper.runAfterDelay(3, () -> {
            requester.triggerRequest();
            helper.assertValueEqual(network.storage().getItem(0).getCount(), 5,
                    "partial-disabled request extracted stock");
            helper.assertTrue(network.packager().heldBox.isEmpty(),
                    "partial-disabled request produced a package");
            helper.assertFalse(requester.lastRequestSucceeded,
                    "partial-disabled request reported success");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "redstone_requester_e2e", timeoutTicks = 100)
    public static void mixedItemFluidRequestCreatesBoxAndJar(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        var iron = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, frequency,
                new ItemStack(Items.IRON_INGOT, 6));
        var water = FactoryPanelRecipeFixture.placeFluidNetwork(helper, 3, frequency,
                new FluidStack(Fluids.WATER, 4000));
        GenericOrder order = GenericOrder.order(List.of(
                GenericStack.wrap(new ItemStack(Items.IRON_INGOT, 2)),
                FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1000))));
        RedstoneRequesterBlockEntity requester = placeRequester(helper, frequency, order,
                "mixed_destination", false);

        helper.runAfterDelay(3, () -> {
            water.packager().drainInventory.findNewCapability();
            requester.triggerRequest();
            helper.assertValueEqual(iron.storage().getItem(0).getCount(), 4, "remaining iron");
            helper.assertValueEqual(water.tank().getFluidInTank(0).getAmount(), 3000,
                    "remaining water");
            assertItemPackage(helper, iron.packager().heldBox, Items.IRON_INGOT, 2,
                    "mixed_destination");
            ItemStack jar = water.packager().heldBox;
            helper.assertTrue(jar.getItem() instanceof JarPackageItem, "fluid output is not a jar");
            helper.assertValueEqual(FluidUtil.getFluidContained(jar).orElse(FluidStack.EMPTY).getAmount(),
                    1000, "jar amount");
            helper.assertTrue(requester.lastRequestSucceeded, "mixed request did not succeed");
            helper.succeed();
        });
    }

    private static RedstoneRequesterBlockEntity placeRequester(GameTestHelper helper, UUID frequency,
                                                                GenericOrder order, String address,
                                                                boolean allowPartial) {
        helper.setBlock(REQUESTER, AllBlocks.REDSTONE_REQUESTER.getDefaultState()
                .setValue(RedstoneRequesterBlock.POWERED, false)
                .setValue(RedstoneRequesterBlock.AXIS, Direction.Axis.X));
        RedstoneRequesterBlockEntity requester = helper.getBlockEntity(REQUESTER);
        requester.behaviour.freqId = frequency;
        requester.encodedTargetAdress = address;
        requester.allowPartialRequests = allowPartial;
        ((GenericRedstoneRequester) requester).setOrder(order);
        return requester;
    }

    private static void assertItemPackage(GameTestHelper helper, ItemStack box,
                                          net.minecraft.world.item.Item item, int amount,
                                          String address) {
        helper.assertFalse(box.isEmpty(), "packager produced no package");
        helper.assertValueEqual(PackageItem.getAddress(box), address, "package address");
        ItemStackHandler contents = PackageItem.getContents(box);
        int found = 0;
        for (int slot = 0; slot < contents.getSlots(); slot++)
            if (contents.getStackInSlot(slot).is(item))
                found += contents.getStackInSlot(slot).getCount();
        helper.assertValueEqual(found, amount, "packaged item amount");
    }

    private RedstoneRequesterGameTests() {}
}
