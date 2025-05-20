package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class FluidKeySerializer implements IngredientKeySerializer<FluidIngredientKey> {
    @Override
    public void write(HolderLookup.Provider levelRegistryAccess, FluidIngredientKey key, CompoundTag tag) {
        ResourceLocation resourceLocation = BuiltInRegistries.FLUID.getKey(key.fluid().value());
        tag.putString("id", resourceLocation.toString());
        if (key.components() != null)
            tag.put("Tag", PatchedDataComponentMap.CODEC.encode(key.components(),
                    levelRegistryAccess.createSerializationContext(NbtOps.INSTANCE),
                    new CompoundTag()).getOrThrow());
    }

    @Override
    public void write(FluidIngredientKey key, RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(BuiltInRegistries.FLUID.getResourceKey(key.fluid().value()).get());
        DataComponentPatch.STREAM_CODEC.encode(buf, key.components().asPatch());
    }

    @Override
    public FluidIngredientKey read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        String key = tag.getString("id");
        return key.isEmpty() ? new FluidIngredientKey(FluidStack.EMPTY.getFluidHolder(), null) :
                new FluidIngredientKey(levelRegistryAccess.holderOrThrow(BuiltInRegistries.FLUID.getResourceKey(BuiltInRegistries.FLUID.get(ResourceLocation.parse(key))).get()),
                        tag.contains("Tag") ? (PatchedDataComponentMap) PatchedDataComponentMap.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("Tag")).getOrThrow().getFirst() : null);
    }

    @Override
    public FluidIngredientKey read(RegistryFriendlyByteBuf buf) {
        return new FluidIngredientKey(buf.registryAccess()
                .holderOrThrow(BuiltInRegistries.FLUID.getResourceKey(
                        BuiltInRegistries.FLUID.get(buf.readResourceKey(BuiltInRegistries.FLUID.key()))).get()),
                PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, DataComponentPatch.STREAM_CODEC.decode(buf)));
    }
}
