package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.resources.ResourceKey;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericKeyProviderExtension;

import java.util.Optional;

public class ChemicalGenericExtension implements GenericKeyProviderExtension<ChemicalKey, ChemicalStack<?>, Chemical<?>> {
    @Override
    public ChemicalKey defaultKey() {
        return new ChemicalKey(MekanismAPI.EMPTY_GAS);
    }

    @Override
    public ChemicalKey wrap(ChemicalStack<?> chemicalStack) {
        return new ChemicalKey(chemicalStack.getRaw());
    }

    @Override
    public ChemicalKey wrapGeneric(ChemicalStack<?> chemicalStack) {
        // mekanism doesn't have a generic way to group something as number of chemicals is a registry constant
        return new ChemicalKey(chemicalStack.getRaw());
    }

    @Override
    public ChemicalStack<?> unwrap(ChemicalKey key) {
        return key.stack();
    }

    @Override
    public String ingredientTypeUid(ChemicalKey key) {
        return switch (ChemicalType.getTypeFor(key.chemical())) {
            case GAS -> "Gas";
            case INFUSION -> "Infuse Type";
            case PIGMENT -> "Pigment";
            case SLURRY -> "Slurry";
        };
    }

    @Override
    public boolean supportsIngredientTypeUid(String uid) {
        return uid.equals("Gas") || uid.equals("Infuse Type") || uid.equals("Pigment") || uid.equals("Slurry");
    }

    @Override
    public Optional<ResourceKey<Chemical<?>>> resourceKey(ChemicalKey key) {
        //noinspection rawtypes
        Optional optional = switch (ChemicalType.getTypeFor(key.chemical())) {
            case GAS -> MekanismAPI.gasRegistry().getResourceKey((Gas) key.chemical());
            case INFUSION -> MekanismAPI.infuseTypeRegistry().getResourceKey((InfuseType) key.chemical());
            case PIGMENT -> MekanismAPI.pigmentRegistry().getResourceKey((Pigment) key.chemical());
            case SLURRY -> MekanismAPI.slurryRegistry().getResourceKey((Slurry) key.chemical());
        };

        return optional;
    }

    @Override
    public int compare(ChemicalKey a, ChemicalKey b) {
        return a.chemical().getRegistryName().compareTo(b.chemical().getRegistryName());
    }
}
