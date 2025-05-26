package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

public class FluidKeySerializer implements GenericKeySerializer<FluidKey> {
    @Override
    public FluidKey read(CompoundTag tag) {
        String key = tag.getString("id");
        return key.isEmpty() ? new FluidKey(Fluids.EMPTY, null) :
               new FluidKey(ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(key)),
                            tag.contains("Tag") ? tag.getCompound("Tag") : null);
    }

    @Override
    public void write(FluidKey key, CompoundTag tag) {
        ResourceLocation resourceLocation = ForgeRegistries.FLUIDS.getKey(key.fluid());
        tag.putString("id", resourceLocation == null ? "minecraft:air" : resourceLocation.toString());
        if (key.nbt() != null)
            tag.put("Tag", key.nbt().copy());
    }

    @Override
    public FluidKey read(FriendlyByteBuf buf) {
        return new FluidKey(buf.readRegistryIdSafe(Fluid.class), buf.readNbt());
    }

    @Override
    public void write(FluidKey key, FriendlyByteBuf buf) {
        buf.writeRegistryId(ForgeRegistries.FLUIDS, key.fluid());
        buf.writeNbt(key.nbt());
    }
}
