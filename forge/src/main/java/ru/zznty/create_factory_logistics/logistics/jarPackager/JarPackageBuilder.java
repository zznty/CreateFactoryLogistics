package ru.zznty.create_factory_logistics.logistics.jarPackager;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageBuilder;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.config.WorldConfig;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

import java.util.List;

public class JarPackageBuilder implements PackageBuilder {
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
        int amountToAdd = Math.min(WorldConfig.jarCapacity - fluidStack.getAmount(), remainingAmount);
        fluidStack.grow(amountToAdd);
        return remainingAmount - amountToAdd;
    }

    @Override
    public List<GenericStack> content() {
        return List.of(FluidGenericStack.wrap(fluidStack));
    }

    @Override
    public boolean isFull() {
        return fluidStack.getAmount() >= WorldConfig.jarCapacity;
    }

    @Override
    public int maxPerSlot() {
        return WorldConfig.jarCapacity;
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

