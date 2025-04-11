package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.FactoryRecipes;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry;

import java.util.Objects;

import static net.minecraft.world.item.BlockItem.BLOCK_ENTITY_TAG;

public class NetworkLinkQualificationRecipe extends CustomRecipe {
    private final ResourceLocation key;

    public NetworkLinkQualificationRecipe(ResourceLocation p_252125_, ResourceLocation key, CraftingBookCategory p_249010_) {
        super(p_252125_, p_249010_);
        this.key = key;
    }

    @Override
    public boolean matches(CraftingContainer p_44002_, Level p_44003_) {
        for (int i = 0; i < p_44002_.getContainerSize(); i++) {
            if (p_44002_.getItem(i).getItem() == FactoryBlocks.NETWORK_LINK.asItem()) {
                return true;
            }
        }

        return false;
    }

    private @Nullable ResourceLocation test(ItemStack item) {
        return Ingredient.of(tag(key)).test(item) ? key : null;
    }

    public static TagKey<Item> tag(ResourceLocation location) {
        if (!IngredientRegistry.REGISTRY.get().containsKey(location))
            throw new IllegalArgumentException("Location" + location + " does not belong to ingredient types registry");
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),
                CreateFactoryLogistics.resource("network_link_qualifier/" + location.getNamespace() + "/" + location.getPath()));
    }

    @Override
    public ItemStack assemble(CraftingContainer p_44001_, RegistryAccess p_267165_) {
        ItemStack link = null;
        ResourceLocation qualifier = null;
        boolean air = true;

        for (int i = 0; i < p_44001_.getContainerSize(); i++) {
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
            qualifier = IngredientRegistry.REGISTRY.get().getDefaultKey();

        if (link == null || qualifier == null) return ItemStack.EMPTY;

        link = qualifyTo(link, qualifier);

        if (air)
            Objects.requireNonNull(link.getTagElement(BLOCK_ENTITY_TAG)).remove("Freq");

        return link;
    }

    public static @NotNull ItemStack qualifyTo(ItemStack link, ResourceLocation qualifier) {
        link = link.copy();

        CompoundTag tag = link.getOrCreateTagElement(BLOCK_ENTITY_TAG);
        tag.putString(NetworkLinkBlock.INGREDIENT_TYPE, qualifier.toString());
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
        @Override
        public NetworkLinkQualificationRecipe fromJson(ResourceLocation p_44103_, JsonObject p_44104_) {
            return new NetworkLinkQualificationRecipe(p_44103_, ResourceLocation.parse(p_44104_.get("key").getAsString()), CraftingBookCategory.CODEC.byName(p_44104_.get("category").getAsString()));
        }

        @Override
        public @Nullable NetworkLinkQualificationRecipe fromNetwork(ResourceLocation p_44105_, FriendlyByteBuf p_44106_) {
            return new NetworkLinkQualificationRecipe(p_44105_, p_44106_.readResourceLocation(), p_44106_.readEnum(CraftingBookCategory.class));
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44101_, NetworkLinkQualificationRecipe p_44102_) {
            p_44101_.writeResourceLocation(p_44102_.key);
            p_44101_.writeEnum(p_44102_.category());
        }
    }
}
