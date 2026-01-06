package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalGenericStack;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalKey;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelStyles;

import java.util.List;

public class BarrelPackageBuilder implements PackageBuilder {
    private ChemicalStack chemicalStack = ChemicalStack.EMPTY;

    @Override
    public int add(GenericStack content) {
        if (!(content.key() instanceof ChemicalKey chemicalKey))
            throw new IllegalArgumentException("Unsupported content: " + content);

        if (!chemicalStack.isEmpty() && !chemicalStack.is(chemicalKey.stack().getChemical()))
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

        IMekanismChemicalHandler attachment = ContainerType.CHEMICAL.createHandler(barrel);
        if (attachment != null) {
            for (IChemicalTank tank : attachment.getChemicalTanks(null)) {
                tank.setStack(new ChemicalStack(chemicalStack.getChemicalHolder(), chemicalStack.getAmount()));
            }
        }

        return barrel;
    }
}
