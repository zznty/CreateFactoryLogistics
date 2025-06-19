package ru.zznty.create_factory_abstractions.api.generic.search;

import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;

import java.util.List;
import java.util.Set;

public interface CategoriesProvider {
    List<ItemStack> categories();

    Set<Integer> hiddenCategories();

    List<List<BigGenericStack>> currentItemSource();
}
