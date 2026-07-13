package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPromiseQueue;
import ru.zznty.create_factory_abstractions.generic.support.PanelRequestedStacks;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.panel.PanelRequestPlanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FactoryPanelCascadeGameTests {
    @GameTest(template = "empty", batch = "panel_cascade_e2e", timeoutTicks = 200)
    public static void twoLevelItemCascadeRequestsBaseIngredients(GameTestHelper helper) {
        UUID logsFrequency = UUID.randomUUID();
        UUID planksFrequency = UUID.randomUUID();
        UUID resultFrequency = UUID.randomUUID();
        var logs = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, logsFrequency,
                new ItemStack(Items.OAK_LOG, 4));
        var planksOutput = FactoryPanelRecipeFixture.placeItemNetwork(helper, 3, planksFrequency,
                ItemStack.EMPTY);
        var resultOutput = FactoryPanelRecipeFixture.placeItemNetwork(helper, 6, resultFrequency,
                ItemStack.EMPTY);
        var logsPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(0, 1, 5),
                logsFrequency, Items.OAK_LOG.getDefaultInstance(), 1);
        var planksPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(3, 1, 5),
                planksFrequency, Items.OAK_PLANKS.getDefaultInstance(), 4);
        var resultPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(6, 1, 5),
                resultFrequency, Items.CRAFTING_TABLE.getDefaultInstance(), 1);

        helper.runAfterDelay(3, () -> {
            assertRegistered(helper, logsFrequency, logs.link());
            assertRegistered(helper, planksFrequency, planksOutput.link());
            assertRegistered(helper, resultFrequency, resultOutput.link());
            FactoryPanelRecipeFixture.connect(logsPanel.behaviour(), planksPanel.behaviour(), 1);
            planksPanel.behaviour().recipeAddress = "sawmill";
            planksPanel.behaviour().recipeOutput = 4;
            FactoryPanelRecipeFixture.connect(planksPanel.behaviour(), resultPanel.behaviour(), 4);
            FactoryPanelRecipeFixture.activateRecipe(resultPanel.behaviour(), "workbench", 1);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(logs.storage().getItem(0).getCount(), 3, "remaining logs");
                assertItemPackage(helper, logs.packager().heldBox, Items.OAK_LOG, 1, "sawmill");
                assertPromise(helper, planksFrequency,
                        GenericStack.wrap(new ItemStack(Items.OAK_PLANKS)), 4);
                assertPromise(helper, resultFrequency,
                        GenericStack.wrap(new ItemStack(Items.CRAFTING_TABLE)), 1);
                helper.assertTrue(planksOutput.packager().heldBox.isEmpty(),
                        "intermediate output registration packager produced a package");
                helper.assertTrue(resultOutput.packager().heldBox.isEmpty(),
                        "final output registration packager produced a package");
            });
        });
    }

    // Chain: result <- planks <- logs, with logs UNAVAILABLE (empty network). The failure signal must
    // redden every path segment, including the deepest planks<-logs, instead of stopping at the end
    // gauge's direct connection (result<-planks).
    @GameTest(template = "empty", batch = "panel_cascade_e2e", timeoutTicks = 200)
    public static void cascadeFailurePropagatesRedPathToLeaf(GameTestHelper helper) {
        UUID logsFrequency = UUID.randomUUID();
        UUID planksFrequency = UUID.randomUUID();
        UUID resultFrequency = UUID.randomUUID();
        // The logs network holds an unrelated item (not logs) so the leaf hits the "has stock, but not the
        // requested ingredient, and cannot cascade further" branch — the path that previously failed
        // silently and never reddened the planks<-logs connection.
        FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, logsFrequency, new ItemStack(Items.DIRT, 1));
        FactoryPanelRecipeFixture.placeItemNetwork(helper, 3, planksFrequency, ItemStack.EMPTY);
        FactoryPanelRecipeFixture.placeItemNetwork(helper, 6, resultFrequency, ItemStack.EMPTY);
        var logsPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(0, 1, 5),
                logsFrequency, Items.OAK_LOG.getDefaultInstance(), 1);
        var planksPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(3, 1, 5),
                planksFrequency, Items.OAK_PLANKS.getDefaultInstance(), 4);
        var resultPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(6, 1, 5),
                resultFrequency, Items.CRAFTING_TABLE.getDefaultInstance(), 1);

        helper.runAfterDelay(3, () -> {
            FactoryPanelRecipeFixture.connect(logsPanel.behaviour(), planksPanel.behaviour(), 1);
            planksPanel.behaviour().recipeAddress = "sawmill";
            planksPanel.behaviour().recipeOutput = 4;
            FactoryPanelRecipeFixture.connect(planksPanel.behaviour(), resultPanel.behaviour(), 4);
            resultPanel.behaviour().recipeAddress = "workbench";
            resultPanel.behaviour().recipeOutput = 1;
            // Panels are actively requesting (not paused by redstone) during a cascade.
            logsPanel.behaviour().redstonePowered = false;
            planksPanel.behaviour().redstonePowered = false;
            resultPanel.behaviour().redstonePowered = false;

            List<Effect> effects = new ArrayList<>();
            PanelRequestPlanner planner = new PanelRequestPlanner(resultPanel.behaviour(),
                    (from, to, success) -> effects.add(new Effect(from, to, success)));

            List<PanelRequestedStacks> toRequest = new ArrayList<>();
            Set<FactoryPanelPosition> visited = new HashSet<>();
            for (FactoryPanelConnection connection : resultPanel.behaviour().targetedBy.values())
                planner.requestDependent(toRequest, connection, resultPanel.behaviour(), visited);

            FactoryPanelPosition logsPos = logsPanel.behaviour().getPanelPosition();
            FactoryPanelPosition planksPos = planksPanel.behaviour().getPanelPosition();
            FactoryPanelPosition resultPos = resultPanel.behaviour().getPanelPosition();

            helper.assertTrue(
                    effects.stream().anyMatch(e -> e.from.equals(planksPos) && e.to.equals(resultPos) && !e.success),
                    "failure path on the end gauge's direct connection (result<-planks) was not emitted");
            helper.assertTrue(
                    effects.stream().anyMatch(e -> e.from.equals(logsPos) && e.to.equals(planksPos) && !e.success),
                    "failure path did not propagate to the leaf connection (planks<-logs)");
            helper.assertFalse(
                    effects.stream().anyMatch(e -> e.success),
                    "no success path should be emitted when the cascade cannot be fulfilled");

            // Simulate the client receiving the leaf effect (FactoryPanelEffectPacket#handle) and verify the
            // condition FactoryPanelRenderer#renderPath needs to tint the planks<-logs arrow red.
            // Intermediate gauges with amount=0 are permanently satisfied=true on the server, but the
            // FactoryPanelRendererMixin overrides satisfied→false at render-time when connection.success
            // is false, so the gating fields below must all resolve in the non-blocking state.
            var planks = planksPanel.behaviour();
            planks.bulb.setValue(1);                                   // effect: panelBehaviour.bulb.setValue(1)
            planks.targetedBy.get(logsPos).success = false;            // effect: connection.success = success
            boolean canRenderRed = !planks.redstonePowered && !planks.waitingForNetwork
                    && planks.bulb.getValue(1f) > 0
                    && !planks.targetedBy.get(logsPos).success;
            helper.assertTrue(canRenderRed,
                    "planks<-logs render red is blocked (redstone=" + planks.redstonePowered
                            + " waiting=" + planks.waitingForNetwork + " satisfied=" + planks.satisfied
                            + " glow=" + planks.bulb.getValue(1f)
                            + " success=" + planks.targetedBy.get(logsPos).success + ")");
            helper.succeed();
        });
    }

    private record Effect(FactoryPanelPosition from, FactoryPanelPosition to, boolean success) {}

    @GameTest(template = "empty", batch = "panel_cascade_e2e", timeoutTicks = 80)
    public static void cascadeCycleProducesNoPackages(GameTestHelper helper) {
        UUID firstFrequency = UUID.randomUUID();
        UUID secondFrequency = UUID.randomUUID();
        var firstNetwork = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, firstFrequency,
                ItemStack.EMPTY);
        var secondNetwork = FactoryPanelRecipeFixture.placeItemNetwork(helper, 3, secondFrequency,
                ItemStack.EMPTY);
        var first = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(0, 1, 5),
                firstFrequency, Items.IRON_INGOT.getDefaultInstance(), 1);
        var second = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(3, 1, 5),
                secondFrequency, Items.GOLD_INGOT.getDefaultInstance(), 1);

        helper.runAfterDelay(3, () -> {
            FactoryPanelRecipeFixture.connect(first.behaviour(), second.behaviour(), 1);
            FactoryPanelRecipeFixture.connect(second.behaviour(), first.behaviour(), 1);
            first.behaviour().recipeAddress = "cycle_a";
            first.behaviour().recipeOutput = 1;
            second.behaviour().recipeAddress = "cycle_b";
            second.behaviour().recipeOutput = 1;
            first.behaviour().redstonePowered = false;
            first.behaviour().tick();
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(firstNetwork.packager().heldBox.isEmpty(), "cycle produced first package");
                helper.assertTrue(secondNetwork.packager().heldBox.isEmpty(), "cycle produced second package");
                helper.succeed();
            });
        });
    }

    private static void assertRegistered(GameTestHelper helper, UUID frequency,
                                         com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity link) {
        helper.assertTrue(LogisticallyLinkedBehaviour.getAllPresent(frequency, false)
                .contains(link.behaviour), "stock link was not registered");
        helper.assertTrue(Create.LOGISTICS.getQueuedPromises(frequency) != null,
                "network has no promise queue");
    }

    private static void assertItemPackage(GameTestHelper helper, ItemStack box,
                                          net.minecraft.world.item.Item item, int count,
                                          String address) {
        helper.assertFalse(box.isEmpty(), "packager produced no package");
        helper.assertValueEqual(PackageItem.getAddress(box), address, "package address");
        ItemStackHandler contents = PackageItem.getContents(box);
        int found = 0;
        for (int slot = 0; slot < contents.getSlots(); slot++)
            if (contents.getStackInSlot(slot).is(item))
                found += contents.getStackInSlot(slot).getCount();
        helper.assertValueEqual(found, count, "packaged item count");
        helper.assertTrue(GenericOrder.of(helper.getLevel().registryAccess(), box) != null,
                "package has no order context");
    }

    private static void assertPromise(GameTestHelper helper, UUID frequency,
                                      GenericStack stack, int amount) {
        var queue = Create.LOGISTICS.getQueuedPromises(frequency);
        helper.assertTrue(queue != null, "promise queue is missing");
        helper.assertValueEqual(((GenericPromiseQueue) queue)
                .getTotalPromisedAndRemoveExpired(stack, -1), amount, "promise amount");
    }

    private FactoryPanelCascadeGameTests() {}
}
