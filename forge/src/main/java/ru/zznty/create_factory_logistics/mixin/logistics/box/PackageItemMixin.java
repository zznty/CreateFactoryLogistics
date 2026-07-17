package ru.zznty.create_factory_logistics.mixin.logistics.box;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zznty.create_factory_abstractions.api.generic.capability.GenericInventory;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericTooltipHelper;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.logistics.box.AbstractPackageItem;

import java.util.List;

@Mixin(PackageItem.class)
public class PackageItemMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void createFactoryLogistics$fireTooltipEvent(ItemStack stack, Item.TooltipContext tooltipContext,
                                                         List<Component> tooltipComponents, TooltipFlag tooltipFlag,
                                                         CallbackInfo ci) {
        if ((Object) this instanceof AbstractPackageItem)
            return;

        List<GenericStack> stacks = GenericInventory.collectStacks(stack, tooltipContext.registries());
        if (!stacks.isEmpty())
            GenericTooltipHelper.fireTooltip(stacks, tooltipComponents);
    }
}
