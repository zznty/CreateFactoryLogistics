package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_abstractions.api.generic.crafting.OrderProvider;
import ru.zznty.create_factory_abstractions.api.generic.crafting.RecipeRequestHelper;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> implements OrderProvider {
    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Shadow
    private InventorySummary forcedEntries;

    @Shadow
    public List<BigGenericStack> itemsToOrder;

    @Shadow
    StockTickerBlockEntity blockEntity;

    @Shadow
    public List<List<BigItemStack>> currentItemSource;

    @Shadow
    public List<CraftableGenericStack> recipesToOrder;

    @Shadow
    private boolean canRequestCraftingPackage, encodeRequester;

    @Shadow
    public AddressEditBox addressBox;

    @Shadow
    private Pair<Integer, List<List<BigItemStack>>> maxCraftable(CraftableBigItemStack cbis, InventorySummary summary,
                                                                 Function<ItemStack, Integer> countModifier,
                                                                 int newTypeLimit) {
        return null;
    }

    @Redirect(
            method = "containerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int getCountInForced(InventorySummary instance, ItemStack $, @Local BigItemStack entry) {
        BigGenericStack stack = BigGenericStack.of(entry);
        return GenericInventorySummary.of(instance).getCountOf(stack.get().key());
    }

    @Redirect(
            method = "containerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;erase(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean eraseFromForced(InventorySummary instance, ItemStack $, @Local BigItemStack entry) {
        BigGenericStack stack = BigGenericStack.of(entry);
        return GenericInventorySummary.of(instance).erase(stack.get().key());
    }

    @Redirect(
            method = "mouseClicked",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack createOrderForIngredientInClicked(ItemStack $, int count,
                                                           @Local(ordinal = 0) BigItemStack entry) {
        BigGenericStack genericStack = BigGenericStack.of(BigGenericStack.of(entry).get().withAmount(1));
        genericStack.setAmount(0);
        return genericStack.asStack();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack createOrderForIngredientInScrolled(ItemStack $, int count,
                                                            @Local(ordinal = 0) BigItemStack entry) {
        BigGenericStack genericStack = BigGenericStack.of(BigGenericStack.of(entry).get().withAmount(1));
        genericStack.setAmount(0);
        return genericStack.asStack();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;getCountOf(Lnet/minecraft/world/item/ItemStack;)I"
            )
    )
    private int getCountInSummary(InventorySummary instance, ItemStack $, @Local(ordinal = 0) BigItemStack entry) {
        BigGenericStack stack = BigGenericStack.of(entry);
        return GenericInventorySummary.of(instance).getCountOf(stack.get().key());
    }

    @Unique
    private BigGenericStack createFactoryLogistics$getOrderForStack(GenericStack stack) {
        for (BigGenericStack entry : itemsToOrder) {
            if (entry.get().canStack(stack))
                return entry;
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
    private BigItemStack getExistingOrderInClicked(StockKeeperRequestScreen instance, ItemStack $,
                                                   @Local BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);
        BigGenericStack order = createFactoryLogistics$getOrderForStack(stack.get());
        return order == null ? null : order.asStack();
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getOrderForItem(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack getExistingOrderInRender(StockKeeperRequestScreen instance, ItemStack $,
                                                  @Local(argsOnly = true) BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);
        BigGenericStack order = createFactoryLogistics$getOrderForStack(stack.get());
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
        BigGenericStack stack = BigGenericStack.of(itemStack);
        return GenericContentExtender.registrationOf(stack.get().key()).clientProvider().guiHandler().stackSize(
                stack.get().key());
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getOrderForItem(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack getExistingOrderInScrolled(StockKeeperRequestScreen instance, ItemStack $,
                                                    @Local BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);
        BigGenericStack order = createFactoryLogistics$getOrderForStack(stack.get());
        return order == null ? null : order.asStack();
    }

    @Overwrite
    private void revalidateOrders() {
        HashSet<BigGenericStack> invalid = new HashSet<>(itemsToOrder);
        GenericInventorySummary summary = GenericInventorySummary.of(
                blockEntity.getLastClientsideStockSnapshotAsSummary());
        if (currentItemSource == null || summary == null) {
            itemsToOrder.removeAll(invalid);
            return;
        }
        for (BigGenericStack stack : itemsToOrder) {
            stack.setAmount(Math.min(summary.getCountOf(stack.get().key()), stack.get().amount()));
            if (stack.get().amount() > 0)
                invalid.remove(stack);
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
    private void renderTooltip(GuiGraphics instance, Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_,
                               @Local BigItemStack itemStack, @Local(ordinal = 1) boolean orderHovered) {
        BigGenericStack stack = BigGenericStack.of(itemStack);
        BigGenericStack order = createFactoryLogistics$getOrderForStack(stack.get());
        int customCount = stack.get().amount();
        if (stack.get().amount() < BigItemStack.INF && !orderHovered) {
            int forcedCount = GenericInventorySummary.of(forcedEntries).getCountOf(stack.get().key());
            if (forcedCount != 0)
                customCount = Math.min(customCount, -forcedCount - 1);
            if (order != null)
                customCount -= order.get().amount();
            customCount = Math.max(0, customCount);
        }

        instance.renderComponentTooltip(p_282308_, GenericContentExtender.registrationOf(
                                                stack.get().key()).clientProvider().guiHandler().tooltipBuilder(stack.get().key(), customCount),
                                        p_282687_, p_282292_);
    }

    @Redirect(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"
            )
    )
    private List<Component> getCraftableTooltip(ItemStack instance, Item.TooltipContext i, Player list,
                                                TooltipFlag tooltipFlag, @Local BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);
        return GenericContentExtender.registrationOf(
                stack.get().key()).clientProvider().guiHandler().tooltipBuilder(stack.get().key(),
                                                                                stack.get().amount());
    }

    // todo make that as proper search
    @WrapOperation(
            method = "refreshSearchResults",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;stack:Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack fluidStackSearchPlaceholder(BigItemStack instance, Operation<ItemStack> original) {
        BigGenericStack stack = BigGenericStack.of(instance);
        if (stack.get().key() instanceof FluidKey fluidKey) {
            return fluidKey.stack().getFluid().getBucket().getDefaultInstance();
        }
        return original.call(instance);
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/gui/element/GuiGameElement;of(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;"
            )
    )
    private GuiGameElement.GuiRenderBuilder renderIngredientEntry(ItemStack itemStack,
                                                                  @Local(argsOnly = true) BigItemStack entry,
                                                                  @Local(argsOnly = true) GuiGraphics graphics) {
        BigGenericStack stack = BigGenericStack.of(entry);
        GenericContentExtender.registrationOf(stack.get().key()).clientProvider().guiHandler()
                .renderSlot(graphics, stack.get().key(), 0, 0);
        return GuiGameElement.of(Blocks.AIR);
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;drawItemCount(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            )
    )
    private void renderIngredientEntryAmount(StockKeeperRequestScreen instance, GuiGraphics graphics, int count,
                                             int customCount,
                                             @Local(argsOnly = true) BigItemStack entry,
                                             @Local(argsOnly = true, ordinal = 0) boolean isStackHovered,
                                             @Local(argsOnly = true, ordinal = 1) boolean isRenderingOrders) {
        // todo workaround amount text rendering over tooltip for order entries
        if (isStackHovered && isRenderingOrders && !(entry instanceof CraftableBigItemStack)) return;
        BigGenericStack stack = BigGenericStack.of(entry);
        count = customCount;
        GenericContentExtender.registrationOf(stack.get().key()).clientProvider().guiHandler()
                .renderDecorations(graphics, stack.get().key(), count, 1, 1);
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
        BigGenericStack stack = BigGenericStack.of(instance);

        stack.setAmount(count);
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
        BigGenericStack stack = BigGenericStack.of(instance);

        stack.setAmount(count);
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
        BigGenericStack stack = BigGenericStack.of(instance);

        stack.setAmount(count);
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
        BigGenericStack stack = BigGenericStack.of(instance);

        stack.setAmount(count);
    }

    @Overwrite
    private void updateCraftableAmounts() {
        canRequestCraftingPackage = RecipeRequestHelper.updateCraftableAmounts(this);
    }

    @WrapOperation(
            method = "sendIt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"
            )
    )
    private void sendRequest(NetworkHelper instance, CustomPacketPayload customPacketPayload, Operation<Void> original,
                             @Local PackageOrderWithCrafts order) {
        ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket packet =
                new ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket(
                        blockEntity.getBlockPos(), GenericOrder.of(order), addressBox.getValue(), encodeRequester);

        instance.sendToServer(packet);
    }

    @WrapOperation(
            method = "removed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
                    ordinal = 0
            )
    )
    private void sendEmptyRequest(NetworkHelper instance, CustomPacketPayload customPacketPayload,
                                  Operation<Void> original) {
        ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket packet =
                new ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket(
                        blockEntity.getBlockPos(), GenericOrder.empty(), addressBox.getValue(), encodeRequester);

        instance.sendToServer(packet);
    }

    @Overwrite(remap = false)
    public void requestCraftable(CraftableBigItemStack cbis, int requestedDifference) {
        RecipeRequestHelper.requestCraftable(this, CraftableGenericStack.of(cbis), requestedDifference);
    }

    @Override
    public List<BigGenericStack> itemsToOrder() {
        return itemsToOrder;
    }

    @Override
    public List<CraftableGenericStack> recipesToOrder() {
        return recipesToOrder;
    }

    @Override
    public Level world() {
        return blockEntity.getLevel();
    }

    @Override
    public BigGenericStack orderForStack(GenericStack stack) {
        return createFactoryLogistics$getOrderForStack(stack);
    }

    @Override
    public GenericInventorySummary stockSnapshot() {
        return GenericInventorySummary.of(blockEntity.getLastClientsideStockSnapshotAsSummary());
    }
}
