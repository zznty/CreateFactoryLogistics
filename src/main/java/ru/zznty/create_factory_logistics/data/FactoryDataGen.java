package ru.zznty.create_factory_logistics.data;

import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.ponder.PonderPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class FactoryDataGen {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new SequencedAssemblyRecipeGen(output));
        generator.addProvider(event.includeServer(), new RecipeQualifierTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new InventoryIdentifierTagsProvider(output, lookupProvider, existingFileHelper));

        CreateFactoryLogistics.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;

            generateLang(langConsumer);
        });
    }

    private static void generateLang(BiConsumer<String, String> consumer) {
        PonderIndex.addPlugin(new PonderPlugin());

        PonderIndex.getLangAccess().provideLang(CreateFactoryLogistics.MODID, consumer);
    }
}
