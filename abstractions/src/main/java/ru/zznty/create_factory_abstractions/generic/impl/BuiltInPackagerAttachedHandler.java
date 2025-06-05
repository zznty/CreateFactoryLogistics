package ru.zznty.create_factory_abstractions.generic.impl;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.List;

@ApiStatus.Internal
public record BuiltInPackagerAttachedHandler(PackagerBlockEntity packagerBE) implements PackagerAttachedHandler {
    @Override
    public int slotCount() {
        return packagerBE.targetInventory.hasInventory() ? packagerBE.targetInventory.getInventory().getSlots() : 0;
    }

    @Override
    public GenericStack extract(int slot, int amount, boolean simulate) {
        if (!packagerBE.targetInventory.hasInventory()) return GenericStack.EMPTY;

        ItemStack extracted = packagerBE.targetInventory.getInventory().extractItem(slot, amount, simulate);

        return GenericStack.wrap(extracted);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side,
                          @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate) {
        if (!PackageItem.isPackage(box))
            return false;

        if (!box.has(AllDataComponents.PACKAGE_CONTENTS)) return false;

        ItemStackHandler contents = PackageItem.getContents(box);
        List<ItemStack> items = ItemHelper.getNonEmptyStacks(contents);
        if (items.isEmpty())
            return true;

        UnpackingHandler handler = UnpackingHandler.REGISTRY.get(state);
        UnpackingHandler toUse = handler != null ? handler : UnpackingHandler.DEFAULT;
        // note: handler may modify the passed items
        return toUse.unpack(level, pos, state, side, items, orderContext, simulate);
    }

    @Override
    public PackageBuilder newPackage() {
        return new BuiltInPackageBuilder();
    }

    @Override
    public boolean hasChanges() {
        return !packagerBE.getBehaviour(VersionedInventoryTrackerBehaviour.TYPE).stillWaiting(
                packagerBE.targetInventory);
    }

    @Override
    public void collectAvailable(GenericInventorySummary summary) {
        if (!packagerBE.targetInventory.hasInventory() || packagerBE.targetInventory.getInventory() instanceof PackagerItemHandler)
            return;

        IItemHandler targetInv = packagerBE.targetInventory.getInventory();

        if (targetInv instanceof BottomlessItemHandler bih) {
            summary.add(GenericStack.wrap(bih.getStackInSlot(0)).withAmount(BigItemStack.INF));
            return;
        }

        for (int slot = 0; slot < targetInv.getSlots(); slot++) {
            int slotLimit = targetInv.getSlotLimit(slot);
            ItemStack stack = targetInv.getStackInSlot(slot);
            summary.add(GenericStack.wrap(stack));
        }

        packagerBE.getBehaviour(VersionedInventoryTrackerBehaviour.TYPE).awaitNewVersion(packagerBE.targetInventory);
    }

    @Override
    public Block supportedGauge() {
        return AllBlocks.FACTORY_GAUGE.get();
    }

    @Override
    public IdentifiedInventory identifiedInventory() {
        if (!packagerBE.targetInventory.hasInventory())
            return null;
        return new IdentifiedInventory(InventoryIdentifier.get(packagerBE.targetInventory.getWorld(),
                                                               packagerBE.targetInventory.getTarget().getOpposite()),
                                       packagerBE.targetInventory.getInventory());
    }
}
