package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public interface GenericAttribute extends ItemAttribute {
    boolean appliesTo(GenericStack stack, Level world);

    default boolean appliesTo(ItemStack stack, Level world) {
        return appliesTo(GenericStack.wrap(stack), world);
    }

    void save(HolderLookup.Provider registries, CompoundTag nbt);

    void load(HolderLookup.Provider registries, CompoundTag nbt);
}
