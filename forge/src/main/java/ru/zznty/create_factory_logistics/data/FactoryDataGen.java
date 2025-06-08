package ru.zznty.create_factory_logistics.data;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.ponder.PonderPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

public class FactoryDataGen {
    public static void gatherDataHighPriority(GatherDataEvent event) {
        REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;

            generateLang(langConsumer);
        });
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(),
                              new FactorySequencedAssemblyRecipeGen(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new ItemTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new BlockTagsProvider(output, lookupProvider, existingFileHelper));

        //noinspection UnstableApiUsage
        event.getGenerator().addProvider(true, REGISTRATE.setDataProvider(
                new RegistrateDataProvider(REGISTRATE, CreateFactoryLogistics.MODID, event)));
    }

    private static void generateLang(BiConsumer<String, String> consumer) {
        PonderIndex.addPlugin(new PonderPlugin());

        PonderIndex.getLangAccess().provideLang(CreateFactoryLogistics.MODID, consumer);
    }
}
