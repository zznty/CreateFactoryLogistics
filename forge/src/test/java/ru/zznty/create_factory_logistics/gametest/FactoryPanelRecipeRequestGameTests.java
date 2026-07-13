package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.attachments.containers.chemical.AttachedChemicals;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.BlockPos;
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
import ru.zznty.create_factory_abstractions.generic.support.GenericPromiseQueue;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismItems;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalGenericStack;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FactoryPanelRecipeRequestGameTests {
    @GameTest(template = "empty", batch = "panel_recipe_e2e", timeoutTicks = 200)
    public static void itemOnlyRecipeRequestsAllIngredients(GameTestHelper helper) {
        UUID diamondsFrequency = UUID.randomUUID();
        UUID redstoneFrequency = UUID.randomUUID();
        UUID outputFrequency = UUID.randomUUID();
        var diamonds = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, diamondsFrequency,
                new ItemStack(Items.DIAMOND, 5));
        var redstone = FactoryPanelRecipeFixture.placeItemNetwork(helper, 3, redstoneFrequency,
                new ItemStack(Items.REDSTONE, 7));
        var outputNetwork = FactoryPanelRecipeFixture.placeItemNetwork(helper, 6, outputFrequency,
                ItemStack.EMPTY);
        var diamondPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(0, 1, 5),
                diamondsFrequency, Items.DIAMOND.getDefaultInstance(), 2);
        var redstonePanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(3, 1, 5),
                redstoneFrequency, Items.REDSTONE.getDefaultInstance(), 3);
        var outputPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(6, 1, 5),
                outputFrequency, Items.COMPARATOR.getDefaultInstance(), 1);

        helper.runAfterDelay(3, () -> {
            assertRegistered(helper, diamondsFrequency, diamonds.link());
            assertRegistered(helper, redstoneFrequency, redstone.link());
            assertRegistered(helper, outputFrequency, outputNetwork.link());
            FactoryPanelRecipeFixture.connect(diamondPanel.behaviour(), outputPanel.behaviour(), 2);
            FactoryPanelRecipeFixture.connect(redstonePanel.behaviour(), outputPanel.behaviour(), 3);
            FactoryPanelRecipeFixture.activateRecipe(outputPanel.behaviour(), "item_assembly", 1);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(diamonds.storage().getItem(0).getCount(), 3,
                        "remaining diamonds");
                helper.assertValueEqual(redstone.storage().getItem(0).getCount(), 4,
                        "remaining redstone");
                assertItemPackage(helper, diamonds.packager().heldBox, Items.DIAMOND, 2,
                        "item_assembly");
                assertItemPackage(helper, redstone.packager().heldBox, Items.REDSTONE, 3,
                        "item_assembly");
                helper.assertTrue(outputNetwork.packager().heldBox.isEmpty(),
                        "output registration packager produced a package");
                assertPromise(helper, outputFrequency,
                        GenericStack.wrap(new ItemStack(Items.COMPARATOR)), 1);
            });
        });
    }

    @GameTest(template = "empty", batch = "panel_recipe_e2e", timeoutTicks = 200)
    public static void mixedItemFluidRecipeRequestsBothPackages(GameTestHelper helper) {
        UUID itemFrequency = UUID.randomUUID();
        UUID fluidFrequency = UUID.randomUUID();
        UUID outputFrequency = UUID.randomUUID();
        var iron = FactoryPanelRecipeFixture.placeItemNetwork(helper, 0, itemFrequency,
                new ItemStack(Items.IRON_INGOT, 6));
        var water = FactoryPanelRecipeFixture.placeFluidNetwork(helper, 3, fluidFrequency,
                new FluidStack(Fluids.WATER, 4000));
        var outputNetwork = FactoryPanelRecipeFixture.placeItemNetwork(helper, 6, outputFrequency,
                ItemStack.EMPTY);
        var ironPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(0, 1, 5),
                itemFrequency, Items.IRON_INGOT.getDefaultInstance(), 2);
        var waterPanel = FactoryPanelRecipeFixture.placeFluidPanel(helper, new BlockPos(3, 1, 5),
                fluidFrequency, Items.WATER_BUCKET.getDefaultInstance(), 1000);
        var outputPanel = FactoryPanelRecipeFixture.placeItemPanel(helper, new BlockPos(6, 1, 5),
                outputFrequency, Items.PISTON.getDefaultInstance(), 1);

        helper.runAfterDelay(3, () -> {
            assertRegistered(helper, itemFrequency, iron.link());
            assertRegistered(helper, fluidFrequency, water.link());
            assertRegistered(helper, outputFrequency, outputNetwork.link());
            water.packager().drainInventory.findNewCapability();
            FactoryPanelRecipeFixture.connect(ironPanel.behaviour(), outputPanel.behaviour(), 2);
            FactoryPanelRecipeFixture.connect(waterPanel.behaviour(), outputPanel.behaviour(), 1000);
            FactoryPanelRecipeFixture.activateRecipe(outputPanel.behaviour(), "hydraulic_assembly", 1);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(iron.storage().getItem(0).getCount(), 4, "remaining iron");
                helper.assertValueEqual(water.tank().getFluidInTank(0).getAmount(), 3000,
                        "remaining water");
                assertItemPackage(helper, iron.packager().heldBox, Items.IRON_INGOT, 2,
                        "hydraulic_assembly");
                assertFluidPackage(helper, water.packager().heldBox, 1000,
                        "hydraulic_assembly");
                helper.assertTrue(outputNetwork.packager().heldBox.isEmpty(),
                        "output registration packager produced a package");
                assertPromise(helper, outputFrequency,
                        GenericStack.wrap(new ItemStack(Items.PISTON)), 1);
            });
        });
    }

    @GameTest(template = "empty", batch = "panel_recipe_e2e", timeoutTicks = 200)
    public static void chemicalRecipeRequestsBarrelAndPromise(GameTestHelper helper) {
        UUID hydrogenFrequency = UUID.randomUUID();
        UUID oxygenFrequency = UUID.randomUUID();
        var hydrogen = FactoryPanelRecipeFixture.placeChemicalNetwork(helper, 0, hydrogenFrequency,
                MekanismChemicals.HYDROGEN.asStack(4000));
        var outputNetwork = FactoryPanelRecipeFixture.placeItemNetwork(helper, 3, oxygenFrequency,
                ItemStack.EMPTY);
        ItemStack hydrogenFilter = chemicalPackage(MekanismChemicals.HYDROGEN.asStack(1));
        ItemStack oxygenFilter = chemicalPackage(MekanismChemicals.OXYGEN.asStack(1));
        var hydrogenPanel = FactoryPanelRecipeFixture.placeChemicalPanel(helper,
                new BlockPos(0, 1, 5), hydrogenFrequency, hydrogenFilter, 1000);
        var oxygenPanel = FactoryPanelRecipeFixture.placeChemicalPanel(helper,
                new BlockPos(3, 1, 5), oxygenFrequency, oxygenFilter, 500);

        helper.runAfterDelay(3, () -> {
            assertRegistered(helper, hydrogenFrequency, hydrogen.link());
            assertRegistered(helper, oxygenFrequency, outputNetwork.link());
            hydrogen.packager().drainInventory.bypassSidedness();
            hydrogen.packager().drainInventory.findNewCapability();
            FactoryPanelRecipeFixture.connect(hydrogenPanel.behaviour(), oxygenPanel.behaviour(), 1000);
            FactoryPanelRecipeFixture.activateRecipe(oxygenPanel.behaviour(), "chemical_conversion", 500);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(hydrogen.tank().getChemicalTank().getStack().getAmount(),
                        3000L, "remaining hydrogen");
                assertChemicalPackage(helper, hydrogen.packager().heldBox, 1000,
                        "chemical_conversion");
                helper.assertTrue(outputNetwork.packager().heldBox.isEmpty(),
                        "output registration packager produced a package");
                assertPromise(helper, oxygenFrequency,
                        ChemicalGenericStack.wrap(MekanismChemicals.OXYGEN.asStack(500)), 500);
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
        helper.assertFalse(box.isEmpty(), "item packager produced no package");
        helper.assertTrue(PackageItem.isPackage(box), "output is not a package");
        helper.assertValueEqual(PackageItem.getAddress(box), address, "package address");
        ItemStackHandler contents = PackageItem.getContents(box);
        int found = 0;
        for (int slot = 0; slot < contents.getSlots(); slot++)
            if (contents.getStackInSlot(slot).is(item))
                found += contents.getStackInSlot(slot).getCount();
        helper.assertValueEqual(found, count, "packaged item count");
        helper.assertTrue(GenericOrder.of(helper.getLevel().registryAccess(), box) != null,
                "package has no generic order context");
    }

    private static void assertFluidPackage(GameTestHelper helper, ItemStack jar, int amount,
                                           String address) {
        helper.assertTrue(jar.getItem() instanceof JarPackageItem, "fluid output is not a jar");
        helper.assertValueEqual(PackageItem.getAddress(jar), address, "jar address");
        FluidStack contained = FluidUtil.getFluidContained(jar).orElse(FluidStack.EMPTY);
        helper.assertTrue(contained.is(Fluids.WATER), "jar contains wrong fluid");
        helper.assertValueEqual(contained.getAmount(), amount, "jar amount");
        helper.assertTrue(GenericOrder.of(helper.getLevel().registryAccess(), jar) != null,
                "jar has no generic order context");
    }

    private static void assertChemicalPackage(GameTestHelper helper, ItemStack barrel, long amount,
                                              String address) {
        helper.assertTrue(barrel.getItem() instanceof BarrelPackageItem,
                "chemical output is not a barrel");
        helper.assertValueEqual(PackageItem.getAddress(barrel), address, "barrel address");
        var handler = barrel.getCapability(Capabilities.CHEMICAL.item());
        helper.assertTrue(handler != null, "barrel has no chemical capability");
        ChemicalStack contained = handler.getChemicalInTank(0);
        helper.assertTrue(contained.is(MekanismChemicals.HYDROGEN),
                "barrel contains wrong chemical");
        helper.assertValueEqual(contained.getAmount(), amount, "barrel amount");
        helper.assertTrue(GenericOrder.of(helper.getLevel().registryAccess(), barrel) != null,
                "barrel has no generic order context");
    }

    private static void assertPromise(GameTestHelper helper, UUID frequency,
                                      GenericStack expected, int amount) {
        RequestPromiseQueue queue = Create.LOGISTICS.getQueuedPromises(frequency);
        helper.assertTrue(queue != null, "result network has no promise queue");
        helper.assertValueEqual(((GenericPromiseQueue) queue)
                .getTotalPromisedAndRemoveExpired(expected, -1), amount,
                "result promise amount");
    }

    private static ItemStack chemicalPackage(ChemicalStack chemical) {
        ItemStack barrel = FactoryMekanismItems.REGULAR_BARREL.asStack();
        barrel.set(MekanismDataComponents.ATTACHED_CHEMICALS.get(),
                new AttachedChemicals(java.util.List.of(chemical.copy())));
        return barrel;
    }

    private FactoryPanelRecipeRequestGameTests() {}
}
