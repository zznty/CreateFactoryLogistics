package ru.zznty.create_factory_logistics;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

public class FactoryRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateFactoryLogistics.MODID);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<NetworkLinkQualificationRecipe>> NETWORK_LINK_QUALIFICATION =
            REGISTER.register("network_link_qualification", () -> new SimpleCraftingRecipeSerializer<>(NetworkLinkQualificationRecipe::new));
}
