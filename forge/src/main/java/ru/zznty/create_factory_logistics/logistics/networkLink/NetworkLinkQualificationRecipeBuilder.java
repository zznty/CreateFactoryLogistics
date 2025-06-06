package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NetworkLinkQualificationRecipeBuilder extends CraftingRecipeBuilder {
    private final RecipeSerializer<?> serializer;

    public NetworkLinkQualificationRecipeBuilder(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public void save(Consumer<FinishedRecipe> p_126360_, ResourceLocation key) {
        p_126360_.accept(new CraftingRecipeBuilder.CraftingResult(CraftingBookCategory.MISC) {
            public RecipeSerializer<?> getType() {
                return NetworkLinkQualificationRecipeBuilder.this.serializer;
            }

            public ResourceLocation getId() {
                return CreateFactoryLogistics.resource("network_link_qualification_" + key.toDebugFileName());
            }

            @Nullable
            public JsonObject serializeAdvancement() {
                return null;
            }

            public ResourceLocation getAdvancementId() {
                return CreateFactoryLogistics.resource("");
            }

            @Override
            public void serializeRecipeData(JsonObject p_250456_) {
                super.serializeRecipeData(p_250456_);
                p_250456_.addProperty("key", key.toString());
            }
        });
    }
}
