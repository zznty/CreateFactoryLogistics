package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

@Mixin(FactoryPanelConnectionHandler.class)
public class FactoryPanelConnectionHandlerMixin {
    @WrapOperation(
            method = "panelClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;",
                    ordinal = 0
            )
    )
    private static Component ingredientNameFrom(ItemStack instance, Operation<Component> original,
                                                @Local(argsOnly = true) FactoryPanelBehaviour panel) {
        if (panel instanceof GenericFilterProvider filterProvider) {
            GenericKeyRegistration registration = GenericContentExtender.registrationOf(filterProvider.filter().key());
            // we want the name only
            return registration.clientProvider().guiHandler().nameBuilder(filterProvider.filter().key()).component();
        }

        return original.call(instance);
    }

    @WrapOperation(
            method = "panelClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;",
                    ordinal = 1
            )
    )
    private static Component ingredientNameTo(ItemStack instance, Operation<Component> original,
                                              @Local(ordinal = 1) FactoryPanelBehaviour panel) {
        if (panel instanceof GenericFilterProvider filterProvider) {
            GenericKeyRegistration registration = GenericContentExtender.registrationOf(filterProvider.filter().key());
            // we want the name only
            return registration.clientProvider().guiHandler().nameBuilder(filterProvider.filter().key()).component();
        }

        return original.call(instance);
    }
}
