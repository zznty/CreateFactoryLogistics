package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class FluidKeySerializer implements IngredientKeySerializer<FluidIngredientKey> {
    @Override
    public void write(FluidIngredientKey key, CompoundTag tag) {
        ResourceLocation resourceLocation = ForgeRegistries.FLUIDS.getKey(key.fluid());
        tag.putString("id", resourceLocation == null ? "minecraft:air" : resourceLocation.toString());
        if (key.nbt() != null)
            tag.put("Tag", key.nbt().copy());
    }

    @Override
    public void write(FluidIngredientKey key, FriendlyByteBuf buf) {
        buf.writeRegistryId(ForgeRegistries.FLUIDS, key.fluid());
        buf.writeNbt(key.nbt());
    }

    @Override
    public FluidIngredientKey read(CompoundTag tag) {
        String key = tag.getString("id");
        return key.isEmpty() ? new FluidIngredientKey(Fluids.EMPTY, null) :
                new FluidIngredientKey(ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(key)),
                        tag.contains("Tag") ? tag.getCompound("Tag") : null);
    }

    @Override
    public FluidIngredientKey read(FriendlyByteBuf buf) {
        return new FluidIngredientKey(buf.readRegistryIdSafe(Fluid.class), buf.readNbt());
    }
}
