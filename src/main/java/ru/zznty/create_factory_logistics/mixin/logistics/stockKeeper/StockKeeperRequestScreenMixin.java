package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;

import java.util.List;

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
        List<Component> component;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            component = List.of(
                    fluidIngredient.stack().getDisplayName(),
                    Component.empty(),
                    FactoryFluidPanelBehaviour.formatLevel(fluidIngredient.amount(), false)
                            .style(ChatFormatting.GRAY)
                            .component()
            );
        } else {
            component = Screen.getTooltipFromItem(Minecraft.getInstance(), p_282781_);
            component.add(Component.empty());
            component.add(CreateLang.text("x")
                    .add(CreateLang.number(stack.getCount()))
                    .style(ChatFormatting.GRAY)
                    .component());
        }

        instance.renderComponentTooltip(p_282308_, component, p_282687_, p_282292_);
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
            text = FactoryFluidPanelBehaviour.formatLevelShort(count).string();
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

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 1
            ),
            remap = false
    )
    private void decreaseOrderCount(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 4
            ),
            remap = false
    )
    private void increaseOrderCount(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Redirect(
            method = "requestCraftable",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/CraftableBigItemStack;count:I",
                    ordinal = 2
            ),
            remap = false
    )
    private void updateOrderCountCraftable(CraftableBigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Redirect(
            method = "requestCraftable",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 3
            ),
            remap = false
    )
    private void decreaseOrderCountCraftable(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Redirect(
            method = "requestCraftable",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 6
            ),
            remap = false
    )
    private void increaseOrderCountCraftable(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }
}
