package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IIngredientInventorySummary;
import ru.zznty.create_factory_logistics.render.FluidSlotRenderer;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {
    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Nullable
    @Shadow(remap = false)
    private BigItemStack getOrderForItem(ItemStack stack) {
        return null;
    }

    @Shadow(remap = false)
    private InventorySummary forcedEntries;

    @Shadow(remap = false)
    public List<BigItemStack> itemsToOrder;

    @Shadow(remap = false)
    StockTickerBlockEntity blockEntity;

    @Shadow(remap = false)
    public List<List<BigItemStack>> currentItemSource;

    @Redirect(
            method = "containerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int getCountInForced(InventorySummary instance, ItemStack $, @Local BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        return stack.getIngredient().getCountIn(instance);
    }

    @Redirect(
            method = "containerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;erase(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean eraseFromForced(InventorySummary instance, ItemStack $, @Local BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        IIngredientInventorySummary summary = (IIngredientInventorySummary) instance;
        return summary.erase(stack.getIngredient());
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack createOrderForIngredientInClicked(ItemStack $, int count, @Local(ordinal = 0) BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        return BigIngredientStack.of(stack.getIngredient().withAmount(1), 0).asStack();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack createOrderForIngredientInScrolled(ItemStack $, int count, @Local(ordinal = 0) BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        return BigIngredientStack.of(stack.getIngredient().withAmount(1), 0).asStack();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int getCountInSummary(InventorySummary instance, ItemStack $, @Local(ordinal = 0) BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        return stack.getIngredient().getCountIn(instance);
    }

    @Unique
    private BigIngredientStack createFactoryLogistics$getOrderForIngredient(BoardIngredient ingredient) {
        for (BigItemStack entry : itemsToOrder) {
            BigIngredientStack stack = (BigIngredientStack) entry;
            if (stack.getIngredient().canStack(ingredient))
                return stack;
        }
        return null;
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getOrderForItem(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack getExistingOrderInClicked(StockKeeperRequestScreen instance, ItemStack $, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.getIngredient());
        return order == null ? null : order.asStack();
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getOrderForItem(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/BigItemStack;"
            ),
            remap = false
    )
    private BigItemStack getExistingOrderInRender(StockKeeperRequestScreen instance, ItemStack $, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.getIngredient());
        return order == null ? null : order.asStack();
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"
            )
    )
    private int getMaxStackSize(ItemStack instance, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        if (stack.getIngredient() instanceof FluidBoardIngredient) {
            return 1000;
        } else if (stack.getIngredient() instanceof ItemBoardIngredient itemIngredient) {
            return itemIngredient.stack().getMaxStackSize();
        }
        return 0;
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getOrderForItem(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack getExistingOrderInScrolled(StockKeeperRequestScreen instance, ItemStack $, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.getIngredient());
        return order == null ? null : order.asStack();
    }

    @Overwrite(remap = false)
    private void revalidateOrders() {
        Set<BigItemStack> invalid = new HashSet<>(itemsToOrder);
        InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
        if (currentItemSource == null || summary == null) {
            itemsToOrder.removeAll(invalid);
            return;
        }
        for (BigItemStack entry : itemsToOrder) {
            BigIngredientStack stack = (BigIngredientStack) entry;
            stack.setCount(Math.min(stack.getIngredient().getCountIn(summary), stack.getCount()));
            if (stack.getCount() > 0)
                invalid.remove(entry);
        }

        itemsToOrder.removeAll(invalid);
    }

    @Redirect(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"
            )
    )
    private void renderTooltip(GuiGraphics instance, Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.getIngredient());
        int customCount = stack.getCount();
        if (stack.getCount() < BigItemStack.INF) {
            int forcedCount = stack.getIngredient().getCountIn(forcedEntries);
            if (forcedCount != 0)
                customCount = Math.min(customCount, -forcedCount - 1);
            if (order != null)
                customCount -= order.getCount();
            customCount = Math.max(0, customCount);
        }

        List<Component> component;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            component = customCount > 0 ? List.of(
                    fluidIngredient.stack().getDisplayName(),
                    Component.empty(),
                    FactoryFluidPanelBehaviour.formatLevel(customCount, false)
                            .style(ChatFormatting.GRAY)
                            .component()
            ) : List.of(fluidIngredient.stack().getDisplayName());
        } else {
            component = Screen.getTooltipFromItem(Minecraft.getInstance(), p_282781_);
            if (customCount > 0) {
                component.add(Component.empty());
                component.add(CreateLang.text("x")
                        .add(CreateLang.number(customCount))
                        .style(ChatFormatting.GRAY)
                        .component());
            }
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
    private GuiGameElement.GuiRenderBuilder renderIngredientEntry(ItemStack itemStack,
                                                                  Operation<GuiGameElement.GuiRenderBuilder> original,
                                                                  @Local(argsOnly = true) BigItemStack entry,
                                                                  @Local(argsOnly = true) GuiGraphics graphics) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
//            return GuiGameElement.of(fluidIngredient.stack().getFluid().getBucket())
//                    .atLocal(1 / 32f, 1 + 1 / 32f, 2);

            FluidSlotRenderer.renderFluidSlot(graphics, 0, 0, fluidIngredient.stack());

            return GuiGameElement.of(Blocks.AIR);
        }
        return original.call(itemStack);
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;drawItemCount(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            ),
            remap = false
    )
    private void renderIngredientEntryAmount(StockKeeperRequestScreen instance, GuiGraphics graphics, int count, int customCount, @Local(argsOnly = true) BigItemStack entry) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        count = customCount;
        String text = "";
        if (stack.isInfinite())
            text = "\u221E";
        else if (stack.getIngredient() instanceof FluidBoardIngredient)
            text = FactoryFluidPanelBehaviour.formatLevelShort(count).string();
        else if (stack.getIngredient() instanceof ItemBoardIngredient) {
            text = count >= 1000000 ? (count / 1000000) + "m"
                    : count >= 10000 ? (count / 1000) + "k"
                    : count >= 1000 ? ((count * 10) / 1000) / 10f + "k" : count >= 100 ? count + "" : " " + count;
        }

        graphics.drawString(font, text, 19 - 2 - font.width(text), 6 + 3, 16777215, true);
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 1
            )
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
            )
    )
    private void increaseOrderCount(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 1
            )
    )
    private void scrollDecreaseOrderCount(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 4
            )
    )
    private void scrollIncreaseOrderCount(BigItemStack instance, int count) {
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
