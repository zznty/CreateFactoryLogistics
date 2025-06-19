package ru.zznty.create_factory_logistics.mixin.accessor;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StockTickerBlockEntity.class)
public interface StockTickerBlockEntityAccessor {
    @Accessor
    void setPreviouslyUsedAddress(String value);
    @Accessor(remap = false)
    List<ItemStack> getCategories();
}
