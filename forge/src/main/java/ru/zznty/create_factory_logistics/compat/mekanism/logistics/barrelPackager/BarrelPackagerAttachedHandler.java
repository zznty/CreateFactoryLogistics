package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.tier.ChemicalTankTier;
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
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;
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
        return packagerBE.drainInventory.hasInventory() ? packagerBE.drainInventory.getInventory().getChemicalTanks() : 0;
    }

    @Override
    public GenericStack extract(int slot, int amount, boolean simulate) {
        if (!packagerBE.drainInventory.hasInventory()) return GenericStack.EMPTY;

        ChemicalStack existing = packagerBE.drainInventory.getInventory().getChemicalInTank(slot);
        if (existing.isEmpty()) return GenericStack.EMPTY;

        ChemicalStack extracted = packagerBE.drainInventory.getInventory().extractChemical(slot, amount,
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

        ChemicalStack source = FactoryChemicalPanelBehaviour.getChemicalStack(box);

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
    public GenericKeyRegistration supportedKey() {
        return GenericContentExtender.REGISTRATIONS.get(ChemicalKey.class);
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
            identifiedInventory.setCapability(packagerBE.drainInventory.capability(), packagerBE.drainInventory.getInventory());
        }
        return inv;
    }
}
