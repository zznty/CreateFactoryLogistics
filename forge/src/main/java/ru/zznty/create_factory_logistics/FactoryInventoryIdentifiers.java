package ru.zznty.create_factory_logistics;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import static com.simibubi.create.api.packager.InventoryIdentifier.REGISTRY;

public class FactoryInventoryIdentifiers {
    // Load this class

    public static void register() {
        REGISTRY.register(AllBlocks.FLUID_TANK.get(), (level, state, face) -> {
            BlockEntity be = level.getBlockEntity(face.getPos());
            if (!(be instanceof FluidTankBlockEntity tank)) return null;

            BlockPos start = tank.getController();
            BlockPos end = start.offset(new Vec3i(tank.getWidth(), tank.getHeight(), tank.getWidth()));
            return new InventoryIdentifier.Bounds(BoundingBox.fromCorners(start, end));
        });
    }
}
