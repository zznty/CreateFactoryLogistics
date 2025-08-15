package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.FactoryGenericAttributeTypes;

import java.util.List;

public class FluidNoNbtGenericAttribute extends FluidGenericAttribute {
    public FluidNoNbtGenericAttribute(@Nullable FluidKey fluid) {
        super(fluid);
    }

    @Override
    public boolean appliesTo(GenericStack stack, Level world) {
        if (fluid == null) return false;
        if (!(stack.key() instanceof FluidKey fluidKey)) return false;
        return fluid.fluid().equals(fluidKey.fluid());
    }

    @Override
    public ItemAttributeType getType() {
        return FactoryGenericAttributeTypes.IS_FLUID_NO_NBT.get();
    }

    @Override
    public String getTranslationKey() {
        return "is_fluid_no_nbt";
    }

    protected static List<FluidKey> extractUniqueFluids(GenericStack stack, Level level) {
        List<FluidKey> fluids = extractFluids(stack, level);
        return fluids.stream()
                .filter(f -> f.nbt() != null)
                .map(f -> new FluidKey(f.fluid(), null))
                .distinct()
                .toList();
    }

    public static class Type implements GenericAttributeType {
        @Override
        public @NotNull GenericAttribute create() {
            return new FluidNoNbtGenericAttribute(null);
        }

        @Override
        public List<ItemAttribute> getAllAttributes(GenericStack stack, Level level) {
            return extractUniqueFluids(stack, level).stream().<ItemAttribute>map(FluidNoNbtGenericAttribute::new)
                    .toList();
        }
    }
}
