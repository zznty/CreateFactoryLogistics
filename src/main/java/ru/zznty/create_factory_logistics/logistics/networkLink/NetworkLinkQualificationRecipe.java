package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.FactoryRecipes;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientProviders;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry;

import java.util.ArrayList;
import java.util.List;

public class NetworkLinkQualificationRecipe extends CustomRecipe {
    private final ResourceLocation key;

    public NetworkLinkQualificationRecipe(ResourceLocation key, CraftingBookCategory p_249010_) {
        super(p_249010_);
        this.key = key;
    }

    @Override
    public boolean matches(CraftingInput p_44002_, Level p_44003_) {
        List<ItemStack> list = new ArrayList<>(p_44002_.items());
        list.removeIf(ItemStack::isEmpty);
        boolean isEmpty = key.equals(IngredientProviders.EMPTY.getId());
        if (list.size() != (isEmpty ? 1 : 2))
            return false;

        return (isEmpty || list.stream().anyMatch(Ingredient.of(tag(key)))) && list.stream().anyMatch(Ingredient.of(FactoryBlocks.NETWORK_LINK));
    }

    private @Nullable ResourceLocation test(ItemStack item) {
        return Ingredient.of(tag(key)).test(item) ? key : null;
    }

    public static TagKey<Item> tag(ResourceLocation location) {
        if (!IngredientRegistry.REGISTRY.containsKey(location))
            throw new IllegalArgumentException("Location " + location + " does not belong to ingredient types registry");
        return TagKey.create(BuiltInRegistries.ITEM.key(),
                CreateFactoryLogistics.resource("network_link_qualifier/" + location.getNamespace() + "/" + location.getPath()));
    }

    @Override
    public ItemStack assemble(CraftingInput p_44001_, HolderLookup.Provider p_267165_) {
        ItemStack link = null;
        ResourceLocation qualifier = null;
        boolean air = true;

        for (int i = 0; i < p_44001_.size(); i++) {
            if (p_44001_.getItem(i).getItem() == FactoryBlocks.NETWORK_LINK.asItem()) {
                if (link != null) return ItemStack.EMPTY;
                link = p_44001_.getItem(i);
            } else {
                if (!p_44001_.getItem(i).isEmpty()) {
                    air = false;
                    if (qualifier != null) return ItemStack.EMPTY;
                    qualifier = test(p_44001_.getItem(i));
                }
            }
        }

        if (air)
            qualifier = IngredientProviders.EMPTY.getId();

        if (link == null || qualifier == null) return ItemStack.EMPTY;

        link = qualifyTo(link, qualifier);

        if (air)
            CustomData.update(DataComponents.BLOCK_ENTITY_DATA, link, t -> t.remove("Freq"));

        return link;
    }

    public static @NotNull ItemStack qualifyTo(ItemStack link, ResourceLocation qualifier) {
        link = link.copy();

        CustomData.update(DataComponents.BLOCK_ENTITY_DATA, link, t -> {
            BlockEntity.addEntityType(t, FactoryBlockEntities.NETWORK_LINK.get());
            t.putString(NetworkLinkBlock.INGREDIENT_TYPE, qualifier.toString());
        });
        return link;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return p_43999_ * p_44000_ >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FactoryRecipes.NETWORK_LINK_QUALIFICATION.get();
    }

    public ResourceLocation key() {
        return key;
    }

    public static class Serializer implements RecipeSerializer<NetworkLinkQualificationRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, NetworkLinkQualificationRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        // i hate codecs i hate codecs i hate codecs
        public static final MapCodec<NetworkLinkQualificationRecipe> CODEC = RecordCodecBuilder.mapCodec((p_340778_) ->
                p_340778_.group(ResourceLocation.CODEC.fieldOf("key").forGetter(NetworkLinkQualificationRecipe::key),
                        CraftingBookCategory.CODEC.fieldOf("category").forGetter(NetworkLinkQualificationRecipe::category)).apply(p_340778_, NetworkLinkQualificationRecipe::new));

        public static @Nullable NetworkLinkQualificationRecipe fromNetwork(RegistryFriendlyByteBuf p_44106_) {
            return new NetworkLinkQualificationRecipe(p_44106_.readResourceLocation(), p_44106_.readEnum(CraftingBookCategory.class));
        }

        public static void toNetwork(RegistryFriendlyByteBuf p_44101_, NetworkLinkQualificationRecipe p_44102_) {
            p_44101_.writeResourceLocation(p_44102_.key);
            p_44101_.writeEnum(p_44102_.category());
        }

        @Override
        public MapCodec<NetworkLinkQualificationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NetworkLinkQualificationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
