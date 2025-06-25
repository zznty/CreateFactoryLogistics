package ru.zznty.create_factory_logistics;

import com.simibubi.create.api.registry.SimpleRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import ru.zznty.create_factory_logistics.logistics.jar.unpack.JarUnpackingHandler;

import java.util.Optional;

public class FactoryJarUnpackingHandlers {
    public static void register() {

        Optional<Fluid> ceiExperience = Optional.ofNullable(ForgeRegistries.FLUIDS.getValue(
                ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "experience")));
        ceiExperience.ifPresent(fluidExperience -> JarUnpackingHandler.REGISTRY.register(
                fluidExperience, (level, pos, fluid, player) -> {
                    // default is 1mb -> 1xp
                    int experience = fluid.getAmount();

                    ExperienceOrb.award(level, pos.getCenter(), experience);
                    return true;
                }));

        JarUnpackingHandler.REGISTRY.registerProvider(
                SimpleRegistry.Provider.forFluidTag(
                        FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "experience")),
                        (level, pos, fluid, player) -> {
                            // default is 20mb -> 1xp
                            int experience = fluid.getAmount() / 20;

                            ExperienceOrb.award(level, pos.getCenter(), experience);
                            return true;
                        }));
    }
}
