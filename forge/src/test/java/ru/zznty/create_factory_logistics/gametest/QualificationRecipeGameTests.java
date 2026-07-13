package ru.zznty.create_factory_logistics.gametest;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkBlock;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

import java.util.List;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class QualificationRecipeGameTests {
    private static final ResourceLocation EMPTY_KEY =
            ResourceLocation.fromNamespaceAndPath(CreateFactoryAbstractions.ID, "empty");
    private static final ResourceLocation ITEM_KEY =
            ResourceLocation.fromNamespaceAndPath(CreateFactoryAbstractions.ID, "item");
    private static final ResourceLocation FLUID_KEY = CreateFactoryLogistics.resource("fluid");

    @GameTest(template = "empty", batch = "recipes")
    public static void itemAndFluidQualificationRecipesMatch(GameTestHelper helper) {
        ItemStack link = FactoryBlocks.NETWORK_LINK.asStack();
        assertQualification(helper, ITEM_KEY, link, new ItemStack(Items.CHEST));
        assertQualification(helper, FLUID_KEY, link, new ItemStack(Items.BUCKET));
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "recipes")
    public static void emptyQualificationClearsFrequency(GameTestHelper helper) {
        NetworkLinkQualificationRecipe recipe = new NetworkLinkQualificationRecipe(EMPTY_KEY,
                CraftingBookCategory.MISC);
        ItemStack link = NetworkLinkQualificationRecipe.qualifyTo(FactoryBlocks.NETWORK_LINK.asStack(), FLUID_KEY);
        CustomData.update(DataComponents.BLOCK_ENTITY_DATA, link, tag -> tag.putInt("Freq", 123));
        CraftingInput input = CraftingInput.of(1, 1, List.of(link));

        helper.assertTrue(recipe.matches(input, helper.getLevel()), "empty qualification did not match");
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertQualified(helper, result, GenericContentExtender.DEFAULT_KEY);
        CompoundTag data = result.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        helper.assertFalse(data.contains("Freq"), "empty qualification retained frequency");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "recipes")
    public static void emptyQualificationSupportsOneByOneCrafting(GameTestHelper helper) {
        NetworkLinkQualificationRecipe recipe = new NetworkLinkQualificationRecipe(EMPTY_KEY,
                CraftingBookCategory.MISC);
        helper.assertTrue(recipe.canCraftInDimensions(1, 1),
                "one-item qualification recipe does not support a 1x1 grid");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "recipes")
    public static void qualificationRejectsUnknownType(GameTestHelper helper) {
        ResourceLocation unknown = ResourceLocation.fromNamespaceAndPath("missing_mod", "unknown");
        NetworkLinkQualificationRecipe recipe = new NetworkLinkQualificationRecipe(unknown,
                CraftingBookCategory.MISC);
        CraftingInput input = CraftingInput.of(2, 1,
                List.of(FactoryBlocks.NETWORK_LINK.asStack(), new ItemStack(Items.CHEST)));
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "unknown qualification matched");
        helper.assertTrue(recipe.assemble(input, helper.getLevel().registryAccess()).isEmpty(),
                "unknown qualification produced an item");
        helper.succeed();
    }

    private static void assertQualification(GameTestHelper helper, ResourceLocation key,
                                            ItemStack link, ItemStack qualifier) {
        NetworkLinkQualificationRecipe recipe = new NetworkLinkQualificationRecipe(key,
                CraftingBookCategory.MISC);
        CraftingInput input = CraftingInput.of(2, 1, List.of(link.copy(), qualifier));
        helper.assertTrue(recipe.matches(input, helper.getLevel()), key + " qualification did not match");
        assertQualified(helper, recipe.assemble(input, helper.getLevel().registryAccess()), key);
    }

    private static void assertQualified(GameTestHelper helper, ItemStack result, ResourceLocation key) {
        helper.assertTrue(result.is(FactoryBlocks.NETWORK_LINK.asItem()), "result is not a network link");
        CompoundTag data = result.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        helper.assertValueEqual(data.getString(NetworkLinkBlock.INGREDIENT_TYPE), key.toString(),
                "qualified generic type");
        helper.assertTrue(data.contains("id"), "qualified link has no block entity id");
    }

    private QualificationRecipeGameTests() {
    }
}
