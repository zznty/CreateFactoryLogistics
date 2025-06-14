package ru.zznty.create_factory_logistics.mixin.accessor;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StockTickerBlockEntity.class)
public interface StockTickerBlockEntityAccessor {
    @Accessor
    void setPreviouslyUsedAddress(String value);
}
