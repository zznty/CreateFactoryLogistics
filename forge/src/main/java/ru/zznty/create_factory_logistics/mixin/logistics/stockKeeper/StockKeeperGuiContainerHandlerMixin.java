package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.simibubi.create.compat.jei.StockKeeperGuiContainerHandler;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_logistics.logistics.ingredient.ClickableIngredientProvider;

import java.util.Optional;

@Mixin(StockKeeperGuiContainerHandler.class)
public class StockKeeperGuiContainerHandlerMixin {
    @Final
    @Shadow(remap = false)
    private IIngredientManager ingredientManager;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "getClickableIngredientUnderMouse(Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;DD)Ljava/util/Optional;",
            at = @At("HEAD"),
            remap = false,
            cancellable = true
    )
    public void getClickableIngredientUnderMouse(StockKeeperRequestScreen containerScreen,
                                                 double mouseX, double mouseY,
                                                 CallbackInfoReturnable cir) {
        if (containerScreen instanceof ClickableIngredientProvider provider) {
            Pair<GenericKey, Rect2i> pair = provider.getHoveredKey((int) mouseX, (int) mouseY);
            if (pair.getFirst() == GenericKey.EMPTY) cir.setReturnValue(Optional.empty());
            else {
                GenericKeyProvider<GenericKey> keyProvider = GenericContentExtender.registrationOf(
                        pair.getFirst()).provider();
                Optional<IIngredientType<?>> ingredientTypeForUid = ingredientManager.getIngredientTypeForUid(
                        keyProvider.ingredientTypeUid());
                if (ingredientTypeForUid.isEmpty()) return;
                cir.setReturnValue(ingredientManager.createClickableIngredient(ingredientTypeForUid.get(),
                                                                               keyProvider.unwrap(pair.getFirst()),
                                                                               pair.getSecond(), true));
            }
        }
    }
}
