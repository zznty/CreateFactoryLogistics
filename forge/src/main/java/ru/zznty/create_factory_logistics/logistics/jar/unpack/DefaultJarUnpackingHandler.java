package ru.zznty.create_factory_logistics.logistics.jar.unpack;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class DefaultJarUnpackingHandler implements JarUnpackingHandler {
    @Override
    public boolean unpack(ServerLevel level, BlockPos pos, FluidStack fluid, @Nullable Player player) {
        return true;
    }
}
