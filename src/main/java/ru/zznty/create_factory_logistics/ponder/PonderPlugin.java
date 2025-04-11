package ru.zznty.create_factory_logistics.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;

public class PonderPlugin implements net.createmod.ponder.api.registration.PonderPlugin {
    @Override
    public String getModId() {
        return CreateFactoryLogistics.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> registration = helper.withKeyFunction(RegistryEntry::getId);

        registration.forComponents(FactoryBlocks.NETWORK_LINK)
                .addStoryBoard(Scenes.MIXER_UPKEEP, Scenes::mixerUpkeep);
    }
}
