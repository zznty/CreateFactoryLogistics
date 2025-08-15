package ru.zznty.create_factory_logistics.logistics.generic;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public interface GenericAttributeType extends ItemAttributeType {
    @NotNull GenericAttribute create();

    List<ItemAttribute> getAllAttributes(GenericStack stack, Level level);

    default @NotNull ItemAttribute createAttribute() {
        return create();
    }

    default List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
        return getAllAttributes(GenericStack.wrap(stack), level);
    }
}
