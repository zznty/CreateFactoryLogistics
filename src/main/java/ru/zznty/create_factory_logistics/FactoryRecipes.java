package ru.zznty.create_factory_logistics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

public class FactoryRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, CreateFactoryLogistics.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, NetworkLinkQualificationRecipe.Serializer> NETWORK_LINK_QUALIFICATION =
            REGISTER.register("network_link_qualification", NetworkLinkQualificationRecipe.Serializer::new);
}
