package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

import java.util.List;
import java.util.Optional;

@ApiStatus.Internal
public class JarPackagerAttachedHandler implements PackagerAttachedHandler {
    private final JarPackagerBlockEntity packagerBE;

    public JarPackagerAttachedHandler(JarPackagerBlockEntity packagerBE) {
        this.packagerBE = packagerBE;
    }

    @Override
    public int slotCount() {
        return packagerBE.drainInventory.hasInventory() ? packagerBE.drainInventory.getInventory().getTanks() : 0;
    }

    @Override
    public GenericStack extract(int slot, int amount, boolean simulate) {
        if (!packagerBE.drainInventory.hasInventory()) return GenericStack.EMPTY;

        FluidStack existing = packagerBE.drainInventory.getInventory().getFluidInTank(slot);
        if (existing.isEmpty()) return GenericStack.EMPTY;

        FluidStack extracted = packagerBE.drainInventory.getInventory().drain(
                FluidHelper.copyStackWithAmount(existing, amount),
                simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

        return extracted.isEmpty() ? GenericStack.EMPTY : FluidGenericStack.wrap(extracted);
    }

    @Override
    public boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side,
                          @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate) {
        if (!(box.getItem() instanceof JarPackageItem)) return false;

        Optional<FluidStack> source = FluidUtil.getFluidContained(box);
        Optional<IFluidHandler> destination = FluidUtil.getFluidHandler(level, pos, side);

        if (source.isEmpty() || destination.isEmpty()) return false;

        if (destination.get().fill(source.get(), IFluidHandler.FluidAction.SIMULATE) != source.get().getAmount())
            return false;

        if (simulate) return true;

        return destination.get().fill(source.get(), IFluidHandler.FluidAction.EXECUTE) == source.get().getAmount();
    }

    @Override
    public PackageBuilder newPackage() {
        return new JarPackageBuilder();
    }

    @Override
    public boolean hasChanges() {
        return true;
    }

    @Override
    public void collectAvailable(boolean scanInputSlots, GenericInventorySummary summary) {
        if (!packagerBE.drainInventory.hasInventory()) {
            // in case inventory didn't load in the first tick
            packagerBE.drainInventory.findNewCapability();
            if (!packagerBE.drainInventory.hasInventory())
                return;
        }

        IFluidHandler fluidHandler = packagerBE.drainInventory.getInventory();

        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack stack = fluidHandler.getFluidInTank(i);
            if (!stack.isEmpty()) {
                if (!scanInputSlots)
                    stack = fluidHandler.drain(stack, IFluidHandler.FluidAction.SIMULATE);
                if (fluidHandler instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
                    stack.setAmount(BigItemStack.INF);

                summary.add(FluidGenericStack.wrap(stack));
            }
        }
    }

    @Override
    public Block supportedGauge() {
        return FactoryBlocks.FACTORY_FLUID_GAUGE.get();
    }

    @Override
    public @Nullable IdentifiedInventory identifiedInventory() {
        IdentifiedInventory inv = new IdentifiedInventory(InventoryIdentifier.get(packagerBE.drainInventory.getWorld(),
                                                                                  packagerBE.drainInventory.getTarget().getOpposite()),
                                                          null);
        {
            GenericIdentifiedInventory identifiedInventory = GenericIdentifiedInventory.from(inv);
            identifiedInventory.setCapability(Capabilities.FluidHandler.BLOCK,
                                              packagerBE.drainInventory.getInventory());
        }
        return inv;
    }
}

class JarPackageBuilder implements PackageBuilder {
    private FluidStack fluidStack = FluidStack.EMPTY;

    @Override
    public int add(GenericStack content) {
        if (!(content.key() instanceof FluidKey fluidKey))
            throw new IllegalArgumentException("Unsupported content: " + content);

        if (!fluidStack.isEmpty() && !FluidStack.isSameFluidSameComponents(fluidStack, fluidKey.stack()))
            return -1;

        if (fluidStack.isEmpty()) {
            fluidStack = fluidKey.stack().copy();
            fluidStack.setAmount(0);
        }

        int remainingAmount = content.amount();
        int amountToAdd = Math.min(Config.jarCapacity - fluidStack.getAmount(), remainingAmount);
        fluidStack.grow(amountToAdd);
        return remainingAmount - amountToAdd;
    }

    @Override
    public List<GenericStack> content() {
        return List.of(FluidGenericStack.wrap(fluidStack));
    }

    @Override
    public boolean isFull() {
        return fluidStack.getAmount() >= Config.jarCapacity;
    }

    @Override
    public int maxPerSlot() {
        return Config.jarCapacity;
    }

    @Override
    public int slotCount() {
        return 1;
    }

    @Override
    public PackageMeasureResult measure(GenericKey key) {
        if (key instanceof FluidKey) {
            return PackageMeasureResult.BULKY;
        }

        throw new IllegalArgumentException("Unsupported key: " + key);
    }

    @Override
    public ItemStack build() {
        if (fluidStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack jar = new ItemStack(JarStyles.getRandomJar());
        FluidUtil.getFluidHandler(jar).ifPresent(
                handler -> handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE));
        return jar;
    }
}
