package ru.zznty.create_factory_logistics.logistics.jar.unpack;

import com.simibubi.create.api.registry.SimpleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface JarUnpackingHandler {
    SimpleRegistry<Fluid, JarUnpackingHandler> REGISTRY = SimpleRegistry.create();

    JarUnpackingHandler DEFAULT = new DefaultJarUnpackingHandler();

    boolean unpack(ServerLevel level, BlockPos pos, FluidStack fluid, @Nullable Player player);
}
