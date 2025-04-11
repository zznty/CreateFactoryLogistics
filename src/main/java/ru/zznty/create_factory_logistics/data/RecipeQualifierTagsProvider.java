package ru.zznty.create_factory_logistics.data;

import com.simibubi.create.AllBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

import java.util.concurrent.CompletableFuture;

public class RecipeQualifierTagsProvider extends TagsProvider<Item> {
    public RecipeQualifierTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, lookupProvider, CreateFactoryLogistics.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(NetworkLinkQualificationRecipe.tag(IngredientProviders.ITEM.getId()))
                .add(TagEntry.tag(Tags.Items.CHESTS.location()))
                .add(TagEntry.tag(Tags.Items.BARRELS.location()))
                .add(asKey(AllBlocks.CREATIVE_CRATE))
                .add(asKey(AllBlocks.ITEM_VAULT));

        tag(NetworkLinkQualificationRecipe.tag(IngredientProviders.FLUID.getId()))
                .add(asKey(AllBlocks.FLUID_TANK))
                .add(asKey(AllBlocks.CREATIVE_FLUID_TANK))
                .add(asKey(Items.BUCKET))
                .add(asKey(Items.GLASS_BOTTLE));
    }

    private ResourceKey<Item> asKey(ItemLike item) {
        return ForgeRegistries.ITEMS.getResourceKey(item.asItem()).get();
    }

    @Override
    public String getName() {
        return "Create Factory Logistics Recipe Qualifier Tags";
    }
}
