package ru.zznty.create_factory_logistics.logistics.stock;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;

import java.util.List;

public interface IngredientInventorySummary {
    void add(BoardIngredient ingredient);

    void add(IngredientInventorySummary summary);

    int getCountOf(IngredientKey key);

    List<BoardIngredient> get();

    boolean isEmpty();

    int getCountOf(BigIngredientStack stack);

    boolean erase(IngredientKey key);

    CompoundTag write(HolderLookup.Provider registries);

    static InventorySummary read(HolderLookup.Provider registries, CompoundTag tag) {
        IngredientInventorySummary summary = (IngredientInventorySummary) new InventorySummary();

        ListTag listTag = tag.getList("List", Tag.TAG_COMPOUND);
        NBTHelper.iterateCompoundList(listTag, compoundTag -> summary.add(BoardIngredient.read(registries, compoundTag)));
        return (InventorySummary) summary;
    }
}
