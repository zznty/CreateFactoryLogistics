package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.PanelRequestedStacks;
import ru.zznty.create_factory_abstractions.generic.support.StackRequest;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class PanelCraftingContextGameTests {
    @GameTest(template = "empty", batch = "panel_crafting")
    public static void reportsCraftingContext(GameTestHelper helper) {
        GenericStack result = GenericStack.wrap(new ItemStack(Items.CRAFTING_TABLE));
        PanelRequestedStacks plain = new PanelRequestedStacks(result, List.of(), List.of(),
                "assembly", UUID.randomUUID());
        PanelRequestedStacks crafting = new PanelRequestedStacks(result, List.of(),
                List.of(new BigItemStack(new ItemStack(Items.OAK_PLANKS), 1)),
                "assembly", UUID.randomUUID());
        helper.assertFalse(plain.hasCraftingContext(), "empty crafting context is present");
        helper.assertTrue(crafting.hasCraftingContext(), "crafting context is absent");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "panel_crafting")
    public static void convertsContextToGenericCraftingOrder(GameTestHelper helper) {
        GenericStack result = GenericStack.wrap(new ItemStack(Items.CRAFTING_TABLE));
        StackRequest ingredient = new StackRequest(
                GenericStack.wrap(new ItemStack(Items.OAK_PLANKS, 4)), UUID.randomUUID());
        PanelRequestedStacks context = new PanelRequestedStacks(result, List.of(ingredient),
                List.of(new BigItemStack(new ItemStack(Items.OAK_PLANKS), 1)),
                "factory", UUID.randomUUID());
        GenericOrder order = GenericOrder.of(context, List.of(ingredient));

        helper.assertValueEqual(order.stacks(), List.of(ingredient.stack()), "ordered stacks");
        helper.assertValueEqual(order.crafts().size(), 1, "crafting context was discarded");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "panel_crafting")
    public static void extractsRecipeFromRealPanels(GameTestHelper helper) {
        var source = FactoryPanelTestSupport.placeWaterPanel(helper, new net.minecraft.core.BlockPos(0, 1, 0), 250);
        var target = FactoryPanelTestSupport.placePanel(helper, new net.minecraft.core.BlockPos(0, 1, 2));
        helper.assertTrue(target.behaviour().setFilter(Items.LAVA_BUCKET.getDefaultInstance()),
                "lava filter was rejected");
        target.behaviour().recipeOutput = 1000;
        target.behaviour().recipeAddress = "mixing";
        target.behaviour().activeCraftingArrangement = List.of(
                new ItemStack(Items.WATER_BUCKET, 32), ItemStack.EMPTY,
                new ItemStack(Items.GLASS_BOTTLE, 7));
        FactoryPanelTestSupport.connect(source, target, 250);

        PanelRequestedStacks request = PanelRequestedStacks.of(target.behaviour());
        helper.assertValueEqual(request.result().amount(), 1000, "recipe output amount");
        helper.assertValueEqual(request.ingredients().size(), 1, "ingredient count");
        helper.assertValueEqual(request.ingredients().getFirst().stack().amount(), 250,
                "fluid ingredient amount");
        helper.assertValueEqual(request.ingredients().getFirst().network(), source.network(),
                "ingredient network");
        helper.assertValueEqual(request.recipeAddress(), "mixing", "recipe address");
        helper.assertValueEqual(request.craftingContext().size(), 3, "crafting arrangement size");
        helper.assertValueEqual(request.craftingContext().getFirst().count, 1,
                "crafting amount was not normalized");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "panel_crafting")
    public static void differentPanelRecipesRemainDistinct(GameTestHelper helper) {
        GenericStack result = GenericStack.wrap(new ItemStack(Items.CRAFTING_TABLE));
        PanelRequestedStacks first = new PanelRequestedStacks(result,
                List.of(new StackRequest(GenericStack.wrap(new ItemStack(Items.OAK_PLANKS, 4)),
                        UUID.randomUUID())),
                List.of(new BigItemStack(new ItemStack(Items.OAK_PLANKS), 1)),
                "first", UUID.randomUUID());
        PanelRequestedStacks second = new PanelRequestedStacks(result,
                List.of(new StackRequest(GenericStack.wrap(new ItemStack(Items.BIRCH_PLANKS, 4)),
                        UUID.randomUUID())),
                List.of(new BigItemStack(new ItemStack(Items.BIRCH_PLANKS), 1)),
                "second", UUID.randomUUID());
        Map<PanelRequestedStacks, Integer> requests = new HashMap<>();
        requests.put(first, 1);
        requests.put(second, 2);
        helper.assertValueEqual(requests.size(), 2, "different panel recipes collapsed");
        helper.succeed();
    }

    private PanelCraftingContextGameTests() {
    }
}
