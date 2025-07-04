package ru.zznty.create_factory_abstractions.api.generic.capability;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.AbstractionsCapabilities;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.BuiltInPackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

@AutoRegisterCapability
public interface PackagerAttachedHandler {
    int slotCount();

    GenericStack extract(int slot, int amount, boolean simulate);

    boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side,
                   @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate);

    PackageBuilder newPackage();

    boolean hasChanges();

    void collectAvailable(boolean scanInputSlots, GenericInventorySummary summary);

    Block supportedGauge();

    @Nullable IdentifiedInventory identifiedInventory();

    static LazyOptional<PackagerAttachedHandler> get(BlockEntity blockEntity) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return blockEntity.getCapability(AbstractionsCapabilities.PACKAGER_ATTACHED);

        if (blockEntity instanceof PackagerBlockEntity pbe)
            return LazyOptional.of(() -> new BuiltInPackagerAttachedHandler(pbe));

        return LazyOptional.empty();
    }
}
