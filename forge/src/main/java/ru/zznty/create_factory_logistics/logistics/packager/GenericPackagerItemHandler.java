package ru.zznty.create_factory_logistics.logistics.packager;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.List;

public class GenericPackagerItemHandler extends PackagerItemHandler {
    private final PackagerBlockEntity blockEntity;

    public GenericPackagerItemHandler(PackagerBlockEntity blockEntity) {
        super(blockEntity);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.getItem() instanceof CompositePackageItem) {
            RegistryAccess registryAccess = blockEntity.getLevel().registryAccess();
            List<ItemStack> children = CompositePackageItem.getChildren(registryAccess, stack);
            if (!children.isEmpty()) {
                ItemStack reminder = super.insertItem(slot, children.getFirst(), simulate);
                if (reminder.isEmpty()) {
                    children.removeFirst();
                    // discord children of composite box
                    return CompositePackageItem.of(registryAccess,
                                                   PackageItem.containing(PackageItem.getContents(stack)), children);
                } else return stack;
            }
        }
        return super.insertItem(slot, stack, simulate);
    }
}
