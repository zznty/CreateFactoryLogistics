package ru.zznty.create_factory_logistics;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerBlock;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkBlock;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkBlockItem;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkGenerator;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipeBuilder;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlock;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlockItem;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelModel;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

@SuppressWarnings("removal")
public class FactoryBlocks {
    public static final BlockEntry<JarPackagerBlock> JAR_PACKAGER = REGISTRATE.block("jar_packager", JarPackagerBlock::new)
            .transform(BuilderTransformers.packager())
            .recipe((c, b) ->
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FactoryBlocks.JAR_PACKAGER)
                            .unlockedBy("has_" + b.safeName(c.getId()),
                                    DataIngredient.items(AllBlocks.PACKAGER.asItem()).getCritereon(b))
                            .pattern(" c ")
                            .pattern("cCc")
                            .pattern("rir")
                            .define('c', Items.COPPER_INGOT)
                            .define('C', AllBlocks.COPPER_CASING)
                            .define('r', Items.REDSTONE)
                            .define('i', Items.IRON_INGOT)
                            .save(b))
            .register();

    public static final BlockEntry<FactoryFluidPanelBlock> FACTORY_FLUID_GAUGE =
            REGISTRATE.block("factory_fluid_gauge", FactoryFluidPanelBlock::new)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::forceSolidOn)
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
                    .onRegister(CreateRegistrate.blockModel(() -> FactoryFluidPanelModel::new))
                    // todo fuck create display cringe, god its awful
//                    .transform(displaySource(FactoryDisplaySources.FLUID_GAUGE_STATUS))
                    .item(FactoryFluidPanelBlockItem::new)
                    .model(AssetLookup::customItemModel)
                    .recipe((c, b) ->
                            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FactoryBlocks.FACTORY_FLUID_GAUGE, 2)
                                    .unlockedBy("has_" + b.safeName(c.getId()),
                                            DataIngredient.items(AllBlocks.FACTORY_GAUGE.asItem()).getCritereon(b))
                                    .requires(AllBlocks.STOCK_LINK)
                                    .requires(FactoryItems.FLUID_MECHANISM)
                                    .save(b))
                    .build()
                    .register();

    public static final BlockEntry<NetworkLinkBlock> NETWORK_LINK =
            REGISTRATE.block("network_link", NetworkLinkBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE)
                            .sound(SoundType.NETHERITE_BLOCK))
                    .transform(pickaxeOnly())
                    .blockstate(new NetworkLinkGenerator()::generate)
                    .item(NetworkLinkBlockItem::new)
                    .recipe((c, b) -> {
                        for (ResourceLocation key : IngredientRegistry.REGISTRY.get().getKeys()) {
                            new NetworkLinkQualificationRecipeBuilder(FactoryRecipes.NETWORK_LINK_QUALIFICATION.get()).save(b, key);
                        }
                        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FactoryBlocks.NETWORK_LINK, 2)
                                .unlockedBy("has_" + b.safeName(c.getId()),
                                        DataIngredient.items(AllBlocks.STOCK_LINK.asItem()).getCritereon(b))
                                .pattern("t")
                                .pattern("C")
                                .define('t', AllBlocks.STOCK_LINK)
                                .define('C', AllBlocks.BRASS_CASING)
                                .save(b);
                    })
                    .transform(customItemModel("_", "block_vertical"))
                    .register();


    // Load this class

    public static void register() {
    }
}
