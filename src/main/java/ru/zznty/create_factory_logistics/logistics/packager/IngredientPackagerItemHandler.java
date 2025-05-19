package ru.zznty.create_factory_logistics.logistics.packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.List;

public class IngredientPackagerItemHandler extends PackagerItemHandler {
    public IngredientPackagerItemHandler(PackagerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.getItem() instanceof CompositePackageItem) {
            List<ItemStack> children = CompositePackageItem.getChildren(stack);
            if (!children.isEmpty()) {
                ItemStack reminder = super.insertItem(slot, children.get(0), simulate);
                if (reminder.isEmpty()) {
                    children.remove(0);
                    // discord children of composite box
                    return CompositePackageItem.of(PackageItem.containing(PackageItem.getContents(stack)), children);
                } else return stack;
            }
        }
        return super.insertItem(slot, stack, simulate);
    }
}
