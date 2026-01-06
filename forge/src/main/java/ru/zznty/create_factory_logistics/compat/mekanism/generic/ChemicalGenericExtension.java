package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventorySummaryProvider;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.GenericKeyProviderExtension;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericCapabilityWrapperProvider;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackageBuilder;

import java.util.Optional;
import java.util.function.Supplier;

public class ChemicalGenericExtension implements GenericKeyProviderExtension<ChemicalKey, ChemicalStack, Chemical, IChemicalHandler, IChemicalHandler> {
    private final GenericCapabilityWrapperProvider<IChemicalHandler, IChemicalHandler> provider = new GenericCapabilityWrapperProvider<>() {
        @Override
        public BlockCapability<IChemicalHandler, Direction> capability() {
            return Capabilities.CHEMICAL.block();
        }

        @Override
        public ItemCapability<IChemicalHandler, Void> capabilityItem() {
            return Capabilities.CHEMICAL.item();
        }

        @Override
        public IChemicalHandler wrap(GenericInventorySummaryProvider summaryProvider, HolderLookup.Provider registries) {
            return new NetworkChemicalHandler(summaryProvider, registries);
        }

        @Override
        public GenericInventorySummaryProvider unwrap(IChemicalHandler capability) {
            return new ChemicalInventorySummaryProvider(capability);
        }
    };

    @Override
    public ChemicalKey defaultKey() {
        return new ChemicalKey(MekanismAPI.EMPTY_CHEMICAL_HOLDER);
    }

    @Override
    public ChemicalKey wrap(ChemicalStack chemicalStack) {
        return new ChemicalKey(chemicalStack.getChemicalHolder());
    }

    @Override
    public ChemicalKey wrapGeneric(ChemicalStack chemicalStack) {
        // mekanism doesn't use nbt for chemicals
        return new ChemicalKey(chemicalStack.getChemicalHolder());
    }

    @Override
    public ChemicalStack unwrap(ChemicalKey key) {
        return key.stack();
    }

    @Override
    public String ingredientTypeUid(ChemicalKey key) {
        return "chemical";
    }

    @Override
    public boolean supportsIngredientTypeUid(String uid) {
        return uid.equals("chemical");
    }

    @Override
    public Optional<ResourceKey<Chemical>> resourceKey(ChemicalKey key) {
        return Optional.ofNullable(key.chemical().getKey());
    }

    @Override
    public GenericCapabilityWrapperProvider<IChemicalHandler, IChemicalHandler> capabilityWrapperProvider() {
        return provider;
    }

    @Override
    public Supplier<PackageBuilder> packageBuilder() {
        return BarrelPackageBuilder::new;
    }

    @Override
    public int compare(ChemicalKey a, ChemicalKey b) {
        return a.chemical().getRegisteredName().compareTo(b.chemical().getRegisteredName());
    }

    @Override
    public int stackSize(ChemicalKey key) {
        return 1000;
    }

    @Override
    public int maxStackSize(ChemicalKey key) {
        return -1;
    }
}
