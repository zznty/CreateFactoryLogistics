package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FactoryPanelLifecycleGameTests {
    @GameTest(template = "empty", batch = "factory_panels")
    public static void createsFourFluidBehaviours(GameTestHelper helper) {
        var fixture = FactoryPanelTestSupport.placePanel(helper, new BlockPos(0, 1, 0));
        helper.assertValueEqual(fixture.blockEntity().panels.size(),
                FactoryPanelBlock.PanelSlot.values().length, "panel behaviour count");
        for (FactoryPanelBlock.PanelSlot slot : FactoryPanelBlock.PanelSlot.values())
            helper.assertTrue(fixture.blockEntity().panels.get(slot) instanceof FactoryFluidPanelBehaviour,
                    "slot is not a fluid panel: " + slot);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "factory_panels")
    public static void addsAndRemovesSubpanels(GameTestHelper helper) {
        var fixture = FactoryPanelTestSupport.placePanel(helper, new BlockPos(0, 1, 0));
        UUID secondNetwork = UUID.randomUUID();
        FactoryPanelBlock.PanelSlot secondSlot = FactoryPanelBlock.PanelSlot.TOP_RIGHT;
        helper.assertTrue(fixture.blockEntity().addPanel(secondSlot, secondNetwork),
                "second panel was not added");
        helper.assertValueEqual(fixture.blockEntity().activePanels(), 2, "active panels after add");
        helper.assertFalse(fixture.blockEntity().addPanel(secondSlot, UUID.randomUUID()),
                "occupied slot accepted another panel");
        helper.assertTrue(fixture.blockEntity().removePanel(secondSlot), "second panel was not removed");
        helper.assertValueEqual(fixture.blockEntity().activePanels(), 1, "active panels after remove");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "factory_panels")
    public static void acceptsFluidAndRejectsItemFilters(GameTestHelper helper) {
        var fixture = FactoryPanelTestSupport.placePanel(helper, new BlockPos(0, 1, 0));
        helper.assertTrue(fixture.behaviour().setFilter(Items.WATER_BUCKET.getDefaultInstance()),
                "water bucket was rejected");
        helper.assertFalse(fixture.behaviour().getFluid().isEmpty(), "water filter produced no fluid");
        helper.assertFalse(fixture.behaviour().setFilter(Items.DIAMOND.getDefaultInstance()),
                "ordinary item was accepted");
        helper.assertTrue(fixture.behaviour().getFilter().is(Items.WATER_BUCKET),
                "rejected filter changed existing filter");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "factory_panels")
    public static void disablingPanelDisconnectsTarget(GameTestHelper helper) {
        var source = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 0), 1000);
        var target = FactoryPanelTestSupport.placeWaterPanel(helper, new BlockPos(0, 1, 2), 1000);
        FactoryPanelTestSupport.connect(source, target, 250);
        source.behaviour().disable();
        helper.assertFalse(target.behaviour().targetedBy.containsKey(source.behaviour().getPanelPosition()),
                "target retained disabled source");
        helper.succeed();
    }

    private FactoryPanelLifecycleGameTests() {
    }
}
