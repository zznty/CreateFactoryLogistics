package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_logistics.FactoryGenericAttributeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FluidGenericAttribute implements GenericAttribute {
    @Nullable
    protected FluidKey fluid;

    public FluidGenericAttribute(@Nullable FluidKey fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean appliesTo(GenericStack stack, Level world) {
        if (fluid == null) return false;
        if (!(stack.key() instanceof FluidKey fluidKey)) return false;
        return fluid.equals(fluidKey);
    }

    @Override
    public ItemAttributeType getType() {
        return FactoryGenericAttributeTypes.IS_FLUID.get();
    }

    @Override
    public void save(HolderLookup.Provider registries, CompoundTag nbt) {
        if (fluid == null) return;
        GenericContentExtender.REGISTRATIONS.get(FluidKey.class).serializer().write(fluid, registries, nbt);
    }

    @Override
    public void load(HolderLookup.Provider registries, CompoundTag nbt) {
        GenericKeySerializer<FluidKey> serializer = GenericContentExtender.REGISTRATIONS.get(
                FluidKey.class).serializer();
        fluid = serializer.read(registries, nbt);
        if (fluid.fluid() == Fluids.EMPTY) fluid = null;
    }

    @Override
    public String getTranslationKey() {
        return "is_fluid";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if (fluid != null) {
            FluidType fluidType = fluid.fluid().value().getFluidType();
            if (fluid.nbt().isEmpty())
                parameter = fluidType.getDescription().getString();
            else
                parameter = fluidType.getDescription(fluid.stack()).getString();
        }
        return new Object[]{parameter};
    }

    protected static List<FluidKey> extractFluids(GenericStack stack, Level level) {
        if (stack.key() instanceof FluidKey fluidKey) {
            return List.of(fluidKey);
        }
        if ((!(stack.key() instanceof ItemKey itemKey))) return List.of();
        Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(itemKey.stack());
        if (fluidHandler.isPresent()) {
            List<FluidKey> attributes = new ArrayList<>();
            IFluidHandlerItem handlerItem = fluidHandler.get();
            for (int i = 0; i < handlerItem.getTanks(); i++) {
                FluidStack fluidInItem = handlerItem.getFluidInTank(i);
                if (fluidInItem.getFluid() != Fluids.EMPTY) {
                    attributes.add(new FluidKey(fluidInItem.getFluidHolder(), fluidInItem.getComponents()));
                }
            }
            return attributes;
        }
        Pair<FluidStack, ItemStack> emptyResult = GenericItemEmptying.emptyItem(level, itemKey.stack(),
                                                                                true);
        FluidStack resultFluid = emptyResult.getFirst();
        if (resultFluid.isEmpty()) return List.of();
        return List.of(new FluidKey(resultFluid.getFluidHolder(), resultFluid.getComponents()));
    }

    @Override
    public int hashCode() {
        return fluid == null ? 0 : fluid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof FluidGenericAttribute that)
            return Objects.equals(fluid, that.fluid);
        return false;
    }

    public static class Type implements GenericAttributeType {
        @Override
        public @NotNull GenericAttribute create() {
            return new FluidGenericAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(GenericStack stack, Level level) {
            return extractFluids(stack, level).stream().<ItemAttribute>map(FluidGenericAttribute::new)
                    .toList();
        }
    }
}
