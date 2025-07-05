package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlocks;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalGenericStack;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalKey;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelPackageItem;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelStyles;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBehaviour;

import java.util.List;

@ApiStatus.Internal
public class BarrelPackagerAttachedHandler implements PackagerAttachedHandler {
    private final BarrelPackagerBlockEntity packagerBE;

    public BarrelPackagerAttachedHandler(BarrelPackagerBlockEntity packagerBE) {
        this.packagerBE = packagerBE;
    }

    @Override
    public int slotCount() {
        return packagerBE.drainInventory.hasInventory() ? packagerBE.drainInventory.getInventory().getTanks() : 0;
    }

    @Override
    public GenericStack extract(int slot, int amount, boolean simulate) {
        if (!packagerBE.drainInventory.hasInventory()) return GenericStack.EMPTY;

        ChemicalStack<?> existing = packagerBE.drainInventory.getInventory().getChemicalInTank(slot);
        if (existing.isEmpty()) return GenericStack.EMPTY;

        ChemicalStack<?> extracted = packagerBE.drainInventory.getInventory().extractChemical(slot, amount,
                                                                                              simulate ?
                                                                                              Action.SIMULATE :
                                                                                              Action.EXECUTE);

        return extracted.isEmpty() ? GenericStack.EMPTY : ChemicalGenericStack.wrap(extracted);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side,
                          @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate) {
        if (!(box.getItem() instanceof BarrelPackageItem)) return false;

        ChemicalStack<?> source = FactoryChemicalPanelBehaviour.getChemicalStack(box);

        if (source.isEmpty() || !packagerBE.drainInventory.hasInventory()) return false;

        IChemicalHandler destination = packagerBE.drainInventory.getInventory();

        if (!destination.insertChemical(source, Action.SIMULATE).isEmpty())
            return false;

        if (simulate) return true;

        return !destination.insertChemical(source, Action.SIMULATE).isEmpty();
    }

    @Override
    public PackageBuilder newPackage() {
        return new BarrelPackageBuilder();
    }

    @Override
    public boolean hasChanges() {
        return true;
    }

    @Override
    public void collectAvailable(boolean scanInputSlots, GenericInventorySummary summary) {
        if (!packagerBE.drainInventory.hasInventory()) {
            // in case inventory didn't load in the first tick
            packagerBE.drainInventory.findNewActive();
            if (!packagerBE.drainInventory.hasInventory())
                return;
        }

        IChemicalHandler<?, ?> inventory = packagerBE.drainInventory.getInventory();

        for (int i = 0; i < inventory.getTanks(); i++) {
            ChemicalStack<?> stack = inventory.getChemicalInTank(i);
            if (!stack.isEmpty()) {
                if (!scanInputSlots)
                    stack = inventory.extractChemical(i, stack.getAmount(), Action.SIMULATE);

                summary.add(ChemicalGenericStack.wrap(stack));
            }
        }
    }

    @Override
    public Block supportedGauge() {
        return FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE.get();
    }

    @Override
    public @Nullable IdentifiedInventory identifiedInventory() {
        IdentifiedInventory inv = new IdentifiedInventory(InventoryIdentifier.get(packagerBE.drainInventory.getWorld(),
                                                                                  CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing()
                                                                                          .getTarget(
                                                                                                  packagerBE.getLevel(),
                                                                                                  packagerBE.getBlockPos(),
                                                                                                  packagerBE.getBlockState()).getOpposite()),
                                                          null);
        {
            GenericIdentifiedInventory identifiedInventory = GenericIdentifiedInventory.from(inv);
            if (packagerBE.drainInventory.hasInventory())
                identifiedInventory.setCapability(packagerBE.drainInventory.getActive().capability(),
                                                  packagerBE.drainInventory.getInventory());
            else
                identifiedInventory.setCapability(Capabilities.GAS_HANDLER, null);
        }
        return inv;
    }
}

class BarrelPackageBuilder implements PackageBuilder {
    private ChemicalStack<?> chemicalStack = MekanismAPI.EMPTY_GAS.getStack(0);

    @Override
    public int add(GenericStack content) {
        if (!(content.key() instanceof ChemicalKey chemicalKey))
            throw new IllegalArgumentException("Unsupported content: " + content);

        if (!chemicalStack.isEmpty() && chemicalStack.getType() != chemicalKey.stack().getType())
            return -1;

        if (chemicalStack.isEmpty()) {
            chemicalStack = chemicalKey.stack().copy();
            chemicalStack.setAmount(0);
        }

        int remainingAmount = content.amount();
        int amountToAdd = Math.min((int) (ChemicalTankTier.BASIC.getStorage() - chemicalStack.getAmount()),
                                   remainingAmount);
        chemicalStack.grow(amountToAdd);
        return remainingAmount - amountToAdd;
    }

    @Override
    public List<GenericStack> content() {
        return List.of(ChemicalGenericStack.wrap(chemicalStack));
    }

    @Override
    public boolean isFull() {
        return chemicalStack.getAmount() >= ChemicalTankTier.BASIC.getStorage();
    }

    @Override
    public int maxPerSlot() {
        return (int) ChemicalTankTier.BASIC.getStorage();
    }

    @Override
    public int slotCount() {
        return 1;
    }

    @Override
    public PackageMeasureResult measure(GenericKey key) {
        if (key instanceof ChemicalKey) {
            return PackageMeasureResult.BULKY;
        }

        throw new IllegalArgumentException("Unsupported key: " + key);
    }

    @Override
    public ItemStack build() {
        if (chemicalStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack barrel = new ItemStack(BarrelStyles.getRandomBarrel());

        return ChemicalUtil.getFilledVariant(barrel, chemicalStack.getAmount(), chemicalStack.getType());
    }
}
