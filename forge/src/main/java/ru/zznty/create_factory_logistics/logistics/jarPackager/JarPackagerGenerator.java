package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerGenerator;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class JarPackagerGenerator extends PackagerGenerator {
    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        String suffix = state.getOptionalValue(PackagerBlock.LINKED)
                .orElse(false) ? "linked" : state.getValue(PackagerBlock.POWERED) ? "powered" : "";
        return AssetLookup.partialBaseModel(ctx, prov, suffix);
    }
}
