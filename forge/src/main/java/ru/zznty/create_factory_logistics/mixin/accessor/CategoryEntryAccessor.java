package ru.zznty.create_factory_logistics.mixin.accessor;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StockKeeperRequestScreen.CategoryEntry.class)
public interface CategoryEntryAccessor {
    @Accessor(remap = false)
    boolean getHidden();

    @Accessor(remap = false)
    void setHidden(boolean hidden);
}
