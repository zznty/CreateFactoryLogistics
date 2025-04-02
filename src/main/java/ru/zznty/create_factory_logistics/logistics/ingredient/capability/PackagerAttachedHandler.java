package ru.zznty.create_factory_logistics.logistics.ingredient.capability;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

@AutoRegisterCapability
public interface PackagerAttachedHandler {
    int slotCount();

    BoardIngredient extract(int slot, int amount, boolean simulate);

    boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side, @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate);

    PackageBuilder newPackage();

    boolean hasChanges();

    void collectAvailable(boolean scanInputSlots, IngredientInventorySummary summary);

    Block supportedGauge();

    @Nullable IdentifiedInventory identifiedInventory();
}
