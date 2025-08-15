package ru.zznty.create_factory_logistics.mixin.logistics.filter;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.GenericAttribute;

import java.util.List;

@Mixin(FilterItemStack.AttributeFilterItemStack.class)
public class GenericAttributeFilterItemStackMixin extends FilterItemStack {
    @Shadow(remap = false)
    public FilterItemStack.AttributeFilterItemStack.WhitelistMode whitelistMode;

    @Shadow(remap = false)
    public List<Pair<ItemAttribute, Boolean>> attributeTests;

    protected GenericAttributeFilterItemStackMixin(ItemStack filter) {
        super(filter);
    }

    @Overwrite(remap = false)
    public boolean test(Level world, FluidStack stack, boolean matchNBT) {
        if (attributeTests.isEmpty())
            return super.test(world, stack, matchNBT);
        GenericStack genericStack = FluidGenericStack.wrap(stack);
        for (Pair<ItemAttribute, Boolean> test : attributeTests) {
            ItemAttribute itemAttribute = test.getFirst();
            boolean inverted = test.getSecond();

            if (!(itemAttribute instanceof GenericAttribute attribute)) continue;

            boolean matches = attribute.appliesTo(genericStack, world) != inverted;

            if (matches) {
                switch (whitelistMode) {
                    case BLACKLIST -> {
                        return false;
                    }
                    case WHITELIST_CONJ -> {
                        continue;
                    }
                    case WHITELIST_DISJ -> {
                        return true;
                    }
                }
            } else {
                switch (whitelistMode) {
                    case BLACKLIST, WHITELIST_DISJ -> {
                        continue;
                    }
                    case WHITELIST_CONJ -> {
                        return false;
                    }
                }
            }
        }

        return switch (whitelistMode) {
            case BLACKLIST, WHITELIST_CONJ -> true;
            case WHITELIST_DISJ -> false;
        };
    }
}
