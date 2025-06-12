package ru.zznty.create_factory_logistics.logistics.generic;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

public class FluidKeySerializer implements GenericKeySerializer<FluidKey> {
    @Override
    public FluidKey read(HolderLookup.Provider registries, CompoundTag tag) {
        String key = tag.getString("id");
        return key.isEmpty() ? new FluidKey(Fluids.EMPTY.builtInRegistryHolder(), null) :
               new FluidKey(BuiltInRegistries.FLUID.getHolder(ResourceLocation.parse(key)).get(),
                            tag.contains("Tag") ?
                            PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY,
                                                              CatnipCodecUtils.decode(DataComponentPatch.CODEC,
                                                                                      tag.getCompound("Tag")).orElse(
                                                                      DataComponentPatch.EMPTY)) :
                            new PatchedDataComponentMap(DataComponentMap.EMPTY));
    }

    @Override
    public void write(FluidKey key, HolderLookup.Provider registries, CompoundTag tag) {
        @Nullable ResourceKey<Fluid> resourceKey = key.fluid().getKey();
        tag.putString("id", resourceKey == null ? "minecraft:air" : resourceKey.location().toString());
        CatnipCodecUtils.encode(DataComponentPatch.CODEC, key.nbt().asPatch())
                .ifPresent(t -> tag.put("Tag", t));
    }

    @Override
    public FluidKey read(RegistryFriendlyByteBuf buf) {
        Holder<Fluid> fluid = BuiltInRegistries.FLUID.getHolderOrThrow(
                buf.readResourceKey(BuiltInRegistries.FLUID.key()));
        DataComponentPatch nbt = DataComponentPatch.STREAM_CODEC.decode(buf);
        return new FluidKey(fluid, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, nbt));
    }

    @Override
    public void write(FluidKey key, RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(key.fluid().getKey());
        DataComponentPatch.STREAM_CODEC.encode(buf, key.nbt().asPatch());
    }
}
