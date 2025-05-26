package ru.zznty.create_factory_abstractions.api.generic.crafting;

import net.minecraft.world.level.Level;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.List;

public interface OrderProvider {
    List<BigGenericStack> itemsToOrder();

    List<CraftableGenericStack> recipesToOrder();

    Level world();

    BigGenericStack orderForStack(GenericStack stack);

    GenericInventorySummary stockSnapshot();
}
