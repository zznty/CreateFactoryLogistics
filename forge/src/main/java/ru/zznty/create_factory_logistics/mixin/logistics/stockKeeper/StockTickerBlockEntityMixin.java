package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericFilterItemStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;

@Mixin(StockTickerBlockEntity.class)
public class StockTickerBlockEntityMixin {
    @Redirect(
            method = "receiveStockPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/filter/FilterItemStack;test(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            remap = false
    )
    private boolean testCategoryFilter(FilterItemStack instance, Level world, ItemStack $,
                                       @Local BigItemStack bigStack) {
        BigGenericStack stack = BigGenericStack.of(bigStack);

        if (instance instanceof GenericFilterItemStack genericFilter) {
            return genericFilter.test(world, stack.get());
        } else {
            if (stack.get().key() instanceof ItemKey itemKey) {
                return instance.test(world, itemKey.stack());
            } else if (stack.get().key() instanceof FluidKey fluidKey) {
                return instance.test(world, fluidKey.stack());
            }
        }

        return false;
    }
}
