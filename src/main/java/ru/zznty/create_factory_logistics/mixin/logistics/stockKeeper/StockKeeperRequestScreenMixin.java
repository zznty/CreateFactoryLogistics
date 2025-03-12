package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;

@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {
    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @WrapOperation(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"
            )
    )
    private void renderTooltip(GuiGraphics instance, Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_, Operation<Void> original, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            instance.renderTooltip(p_282308_, fluidIngredient.stack().getDisplayName(), p_282687_, p_282292_);
        } else {
            original.call(instance, p_282308_, p_282781_, p_282687_, p_282292_);
        }
    }

    // todo make that as proper search
    @WrapOperation(
            method = "refreshSearchResults",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;stack:Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private ItemStack fluidStackSearchPlaceholder(BigItemStack instance, Operation<ItemStack> original) {
        BigIngredientStack stack = (BigIngredientStack) instance;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            return fluidIngredient.stack().getFluid().getBucket().getDefaultInstance();
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/gui/element/GuiGameElement;of(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;"
            ),
            remap = false
    )
    private GuiGameElement.GuiRenderBuilder renderIngredientEntry(ItemStack itemStack, Operation<GuiGameElement.GuiRenderBuilder> original, @Local(argsOnly = true) BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            return GuiGameElement.of(fluidIngredient.stack().getFluid())
                    .scale(15)
                    .atLocal(1 / 32f, 1 + 1 / 32f, 2);
        }
        return original.call(itemStack);
    }

    @WrapOperation(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;drawItemCount(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            ),
            remap = false
    )
    private void renderIngredientEntryAmount(StockKeeperRequestScreen instance, GuiGraphics graphics, int count, int customCount, Operation<Void> original, @Local(argsOnly = true) BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        count = customCount;
        String text = "";
        if (stack.isInfinite())
            text = "+";
        else if (stack.getIngredient() instanceof FluidBoardIngredient)
            text = FactoryFluidPanelBehaviour.formatLevel(count).string();
        else if (stack.getIngredient() instanceof ItemBoardIngredient itemIngredient) {
            if (count > 1000) {
                text = count >= 1000000 ? count / 1000000 + "m"
                        : count >= 10000 ? count / 1000 + "k"
                        : (count * 10 / 1000) / 10f + "k";
            } else {
                int stackSize = itemIngredient.stack().getMaxStackSize();
                int stacks = Mth.floor((float) count / stackSize);
                int remaining = count % stackSize;

                if (remaining > 0) text += remaining;

                if (stacks > 0) {
                    if (remaining > 0) text += "+";
                    text += stacks + "\u25A4";
                }
            }
        }

        graphics.drawString(font, text, 19 - 2 - font.width(text), 6 + 3, 16777215, true);
    }
}
