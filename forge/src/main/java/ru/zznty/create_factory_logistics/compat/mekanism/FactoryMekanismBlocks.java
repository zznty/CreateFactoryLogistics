package ru.zznty.create_factory_logistics.compat.mekanism;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import mekanism.api.MekanismAPI;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.ForgeRegistries;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackagerBlock;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBlock;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelModel;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerGenerator;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

@SuppressWarnings("removal")
public class FactoryMekanismBlocks {
    public static final BlockEntry<FactoryChemicalPanelBlock> FACTORY_CHEMICAL_GAUGE =
            REGISTRATE.block("factory_chemical_gauge", FactoryChemicalPanelBlock::new)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::forceSolidOn)
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
                    .onRegister(CreateRegistrate.blockModel(() -> FactoryChemicalPanelModel::new))
                    .item(FactoryPanelBlockItem::new)
                    .model(AssetLookup::customItemModel)
                    .recipe((c, b) ->
                                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                                               FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE, 2)
                                            .unlockedBy("has_" + b.safeName(c.getId()),
                                                        DataIngredient.items(
                                                                AllBlocks.FACTORY_GAUGE.asItem()).getCritereon(b))
                                            .pattern("s")
                                            .pattern("c")
                                            .pattern("C")
                                            .define('s', AllBlocks.STOCK_LINK)
                                            .define('c', TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),
                                                                       ResourceLocation.fromNamespaceAndPath("forge",
                                                                                                             "circuits/advanced")))
                                            .define('C', ForgeRegistries.ITEMS.getDelegateOrThrow(
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            MekanismAPI.MEKANISM_MODID, "steel_casing")).get())
                                            .save(b))
                    .build()
                    .register();

    public static final BlockEntry<BarrelPackagerBlock> BARREL_PACKAGER = REGISTRATE.block("barrel_packager",
                                                                                           BarrelPackagerBlock::new)
            .transform(BuilderTransformers.packager())
            .blockstate(new JarPackagerGenerator()::generate)
            .recipe((c, b) ->
                            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FactoryMekanismBlocks.BARREL_PACKAGER)
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

    public static void register() {
    }
}
