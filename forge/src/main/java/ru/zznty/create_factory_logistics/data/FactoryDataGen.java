package ru.zznty.create_factory_logistics.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class FactoryDataGen {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(),
                              new FactorySequencedAssemblyRecipeGen(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ItemTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new BlockTagsProvider(output, lookupProvider, existingFileHelper));
    }
}
