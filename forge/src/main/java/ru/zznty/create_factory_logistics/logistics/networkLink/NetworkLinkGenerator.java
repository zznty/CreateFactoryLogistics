package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class NetworkLinkGenerator extends SpecialBlockStateGen {
    @Override
    protected int getXRotation(BlockState state) {
        return state.getValue(NetworkLinkBlock.FACE) == AttachFace.CEILING ? 180 : 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return horizontalAngle(state.getValue(NetworkLinkBlock.FACING));
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        String variant =
                state.getValue(NetworkLinkBlock.FACE) == AttachFace.WALL ? "block_horizontal" : "block_vertical";
        return prov.models()
                .getExistingFile(prov.modLoc("block/network_link/" + variant));
    }
}
