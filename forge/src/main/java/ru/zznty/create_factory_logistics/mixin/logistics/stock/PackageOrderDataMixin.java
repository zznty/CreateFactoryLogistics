package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PackageItem.class)
public class PackageOrderDataMixin {
    @Overwrite
    public static int getOrderId(ItemStack box) {
        CustomData data = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.contains("Fragment"))
            return -1;
        //noinspection deprecation
        return data.getUnsafe()
                .getCompound("Fragment")
                .getInt("OrderId");
    }

    @Overwrite
    public static PackageOrderWithCrafts getOrderContext(ItemStack box) {
        throw new UnsupportedOperationException("Use IngredientOrder instead");
    }
}
