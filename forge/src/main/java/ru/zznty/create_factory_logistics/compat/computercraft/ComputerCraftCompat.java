package ru.zznty.create_factory_logistics.compat.computercraft;

import dan200.computercraft.api.detail.ForgeDetailRegistries;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.compat.computercraft.GenericDetailsProvider;
import ru.zznty.create_factory_abstractions.compat.computercraft.GenericStackParser;
import ru.zznty.create_factory_abstractions.compat.computercraft.LuaNbtUntil;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;

import java.util.Map;
import java.util.Optional;

import static ru.zznty.create_factory_abstractions.compat.computercraft.AbstractionsComputerCraftCompat.parseCount;

public class ComputerCraftCompat {

    public static void register() {
        GenericKeyRegistration fluidReg = GenericContentExtender.REGISTRATIONS.get(FluidKey.class);
        GenericDetailsProvider.REGISTRY.register(fluidReg,
                                                 (GenericDetailsProvider<FluidKey>)
                                                         key -> ForgeDetailRegistries.FLUID_STACK.getDetails(
                                                                 key.stack()));

        GenericStackParser.REGISTRY.register(fluidReg, data -> {
            String name = "minecraft:empty";
            if (data.get("name") instanceof String) {
                name = (String) data.get("name");
            }
            int count = parseCount("count", data);
            if (count < 0)
                count = parseCount("amount", data);
            if (count < 0)
                count = 1;
            ResourceLocation resourceLocation = ResourceLocation.tryParse(name);
            Optional<Holder.Reference<Fluid>> fluid = BuiltInRegistries.FLUID.getHolder(resourceLocation);
            if (fluid.isEmpty()) return GenericStack.EMPTY;

            DataComponentPatch components = DataComponentPatch.EMPTY;
            if (data.get("tag") instanceof Map<?, ?> tagData) {
                CompoundTag tag = LuaNbtUntil.parseTag(tagData);
                components = CatnipCodecUtils.decode(DataComponentPatch.CODEC, tag).orElse(components);
            }

            return FluidGenericStack.wrap(new FluidStack(fluid.get(), 1, components)).withAmount(count);
        });

        ForgeDetailRegistries.FLUID_STACK.addProvider((data, stack) -> {
            if (stack.isComponentsPatchEmpty())
                return;

            CatnipCodecUtils.encode(DataComponentPatch.CODEC, stack.getComponentsPatch())
                    .ifPresent(value -> data.put("tag", LuaNbtUntil.serializeTag(value)));
        });
    }
}
