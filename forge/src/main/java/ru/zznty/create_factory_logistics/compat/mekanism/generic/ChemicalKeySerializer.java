package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

import java.util.Optional;

public class ChemicalKeySerializer implements GenericKeySerializer<ChemicalKey> {
    @Override
    public ChemicalKey read(HolderLookup.Provider registries, CompoundTag tag) {
        String chemical = tag.getString("id");
        if (chemical.isEmpty()) return new ChemicalKey(MekanismAPI.EMPTY_CHEMICAL_HOLDER);
        Optional<Holder.Reference<Chemical>> holder = MekanismAPI.CHEMICAL_REGISTRY.getHolder(ResourceLocation.parse(chemical));
        return holder.map(ChemicalKey::new).orElseGet(() -> new ChemicalKey(MekanismAPI.EMPTY_CHEMICAL_HOLDER));
    }

    @Override
    public void write(ChemicalKey key, HolderLookup.Provider registries, CompoundTag tag) {
        Holder<Chemical> chemical = key.chemical();
        ResourceKey<Chemical> resourceKey = chemical.getKey();
        if (resourceKey == null) return;
        tag.putString("id", resourceKey.location().toString());
    }

    @Override
    public ChemicalKey read(RegistryFriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();
        Optional<Holder.Reference<Chemical>> holder = MekanismAPI.CHEMICAL_REGISTRY.getHolder(key);
        return holder.map(ChemicalKey::new).orElseGet(() -> new ChemicalKey(MekanismAPI.EMPTY_CHEMICAL_HOLDER));
    }

    @Override
    public void write(ChemicalKey key, RegistryFriendlyByteBuf buf) {
        ResourceKey<Chemical> resourceKey = key.chemical().getKey();
        if (resourceKey == null) {
            resourceKey = MekanismAPI.EMPTY_CHEMICAL_KEY;
        }
        buf.writeResourceLocation(resourceKey.location());
    }
}
