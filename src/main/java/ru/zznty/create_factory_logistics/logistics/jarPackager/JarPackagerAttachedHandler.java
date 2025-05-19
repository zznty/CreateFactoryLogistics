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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageBuilder;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageMeasureResult;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidIngredientKey;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientIdentifiedInventory;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

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
    public BoardIngredient extract(int slot, int amount, boolean simulate) {
        if (!packagerBE.drainInventory.hasInventory()) return BoardIngredient.of();

        FluidStack existing = packagerBE.drainInventory.getInventory().getFluidInTank(slot);
        if (existing.isEmpty()) return BoardIngredient.of();

        FluidStack extracted = packagerBE.drainInventory.getInventory().drain(FluidHelper.copyStackWithAmount(existing, amount),
                simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

        return extracted.isEmpty() ? BoardIngredient.of() : new BoardIngredient(IngredientKey.of(extracted), extracted.getAmount());
    }

    @Override
    public boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side, @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate) {
        if (!(box.getItem() instanceof JarPackageItem)) return false;

        Optional<FluidStack> source = FluidUtil.getFluidContained(box);
        Optional<IFluidHandler> destination = FluidUtil.getFluidHandler(level, pos, side).resolve();

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
    public void collectAvailable(boolean scanInputSlots, IngredientInventorySummary summary) {
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

                summary.add(new BoardIngredient(IngredientKey.of(stack), stack.getAmount()));
            }
        }
    }

    @Override
    public Block supportedGauge() {
        return FactoryBlocks.FACTORY_FLUID_GAUGE.get();
    }

    @Override
    public @Nullable IdentifiedInventory identifiedInventory() {
        IdentifiedInventory inv = new IdentifiedInventory(InventoryIdentifier.get(packagerBE.drainInventory.getWorld(), packagerBE.drainInventory.getTarget().getOpposite()), null);
        {
            IngredientIdentifiedInventory identifiedInventory = IngredientIdentifiedInventory.from(inv);
            identifiedInventory.setCapability(ForgeCapabilities.FLUID_HANDLER, packagerBE.drainInventory.getInventory());
        }
        return inv;
    }
}

class JarPackageBuilder implements PackageBuilder {
    private FluidStack fluidStack = FluidStack.EMPTY;

    @Override
    public int add(BoardIngredient content) {
        if (!(content.key() instanceof FluidIngredientKey fluidKey))
            throw new IllegalArgumentException("Unsupported content: " + content);

        if (!fluidStack.isEmpty() && !fluidStack.isFluidEqual(fluidKey.stack()))
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
    public List<BoardIngredient> content() {
        return List.of(new BoardIngredient(IngredientKey.of(fluidStack), fluidStack.getAmount()));
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
    public PackageMeasureResult measure(IngredientKey key) {
        if (key instanceof FluidIngredientKey fluidKey) {
            return PackageMeasureResult.BULKY;
        }

        throw new IllegalArgumentException("Unsupported key: " + key);
    }

    @Override
    public ItemStack build() {
        if (fluidStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack jar = new ItemStack(JarStyles.getRandomJar());
        FluidUtil.getFluidHandler(jar).ifPresent(handler -> handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE));
        return jar;
    }
}
