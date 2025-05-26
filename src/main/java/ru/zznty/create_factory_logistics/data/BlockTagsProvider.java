package ru.zznty.create_factory_logistics.data;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagsProvider extends TagsProvider<Block> {
    public BlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                             @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, lookupProvider, CreateFactoryLogistics.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(AllTags.AllBlockTags.SINGLE_BLOCK_INVENTORIES.tag)
                .add(asKey(Blocks.WATER_CAULDRON), asKey(Blocks.LAVA_CAULDRON))
                .add(AllBlocks.BASIN.getKey())
                .add(FactoryBlocks.NETWORK_LINK.getKey());

        tag(TagKey.create(BuiltInRegistries.BLOCK.key(),
                          CreateFactoryLogistics.resource("packager_item")))
                .add(AllBlocks.PACKAGER.getKey())
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("create_vibrant_vaults", "vibrant_packagers"));
    }

    private ResourceKey<Block> asKey(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).get();
    }
}
