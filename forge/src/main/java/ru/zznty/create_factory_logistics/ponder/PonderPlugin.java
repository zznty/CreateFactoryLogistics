package ru.zznty.create_factory_logistics.ponder;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;

public class PonderPlugin implements net.createmod.ponder.api.registration.PonderPlugin {
    @Override
    public String getModId() {
        return CreateFactoryLogistics.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> registration = helper.withKeyFunction(RegistryEntry::getId);

        registration.forComponents(FactoryBlocks.NETWORK_LINK)
                .addStoryBoard(Scenes.MIXER_UPKEEP, Scenes::mixerUpkeep);

        // The AE2 interface integration scene is keyed on AE2's interface item, so it
        // only makes sense (and only resolves) when AE2 is present.
        if (ModList.get().isLoaded(CreateFactoryLogistics.AE2_ID))
            helper.forComponents(ResourceLocation.fromNamespaceAndPath(CreateFactoryLogistics.AE2_ID, "interface"))
                    .addStoryBoard(Scenes.AE_INTERFACE, Scenes::aeInterface, AllCreatePonderTags.HIGH_LOGISTICS);
    }
}
