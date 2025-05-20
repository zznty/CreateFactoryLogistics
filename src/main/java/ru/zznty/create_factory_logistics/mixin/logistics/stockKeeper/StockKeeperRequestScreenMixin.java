package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.CraftableIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGui;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidIngredientKey;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.*;
import java.util.function.Function;

@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {
    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Shadow
    private InventorySummary forcedEntries;

    @Shadow
    public List<BigItemStack> itemsToOrder;

    @Shadow
    StockTickerBlockEntity blockEntity;

    @Shadow
    public List<List<BigItemStack>> currentItemSource;

    @Shadow
    public List<CraftableBigItemStack> recipesToOrder;

    @Shadow
    private boolean canRequestCraftingPackage, encodeRequester;

    @Shadow
    public AddressEditBox addressBox;

    @Shadow
    private Pair<Integer, List<List<BigItemStack>>> maxCraftable(CraftableBigItemStack cbis, InventorySummary summary,
                                                                 Function<ItemStack, Integer> countModifier, int newTypeLimit) {
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
        BigIngredientStack stack = (BigIngredientStack) entry;
        return ((IngredientInventorySummary) instance).getCountOf(stack);
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
        IngredientInventorySummary summary = (IngredientInventorySummary) instance;
        return summary.erase(stack.ingredient().key());
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
        return BigIngredientStack.of(stack.ingredient().withAmount(1), 0).asStack();
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
        return BigIngredientStack.of(stack.ingredient().withAmount(1), 0).asStack();
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
        return ((IngredientInventorySummary) instance).getCountOf(stack);
    }

    @Unique
    private BigIngredientStack createFactoryLogistics$getOrderForIngredient(BoardIngredient ingredient) {
        for (BigItemStack entry : itemsToOrder) {
            BigIngredientStack stack = (BigIngredientStack) entry;
            if (stack.ingredient().canStack(ingredient))
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
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.ingredient());
        return order == null ? null : order.asStack();
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getOrderForItem(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack getExistingOrderInRender(StockKeeperRequestScreen instance, ItemStack $, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.ingredient());
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
        return IngredientGui.stackSize(stack.ingredient().key());
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
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.ingredient());
        return order == null ? null : order.asStack();
    }

    @Overwrite
    private void revalidateOrders() {
        Set<BigItemStack> invalid = new HashSet<>(itemsToOrder);
        IngredientInventorySummary summary = (IngredientInventorySummary) blockEntity.getLastClientsideStockSnapshotAsSummary();
        if (currentItemSource == null || summary == null) {
            itemsToOrder.removeAll(invalid);
            return;
        }
        for (BigItemStack entry : itemsToOrder) {
            BigIngredientStack stack = (BigIngredientStack) entry;
            stack.setCount(Math.min(summary.getCountOf(stack), stack.getCount()));
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
    private void renderTooltip(GuiGraphics instance, Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_, @Local BigItemStack itemStack, @Local(ordinal = 1) boolean orderHovered) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(stack.ingredient());
        int customCount = stack.getCount();
        if (stack.getCount() < BigItemStack.INF && !orderHovered) {
            int forcedCount = ((IngredientInventorySummary) forcedEntries).getCountOf(stack);
            if (forcedCount != 0)
                customCount = Math.min(customCount, -forcedCount - 1);
            if (order != null)
                customCount -= order.getCount();
            customCount = Math.max(0, customCount);
        }

        instance.renderComponentTooltip(p_282308_, IngredientGui.tooltipBuilder(stack.ingredient().key(), customCount), p_282687_, p_282292_);
    }

    @Redirect(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"
            )
    )
    private List<Component> getCraftableTooltip(ItemStack instance, Item.TooltipContext i, Player list, TooltipFlag tooltipFlag, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        return IngredientGui.tooltipBuilder(stack.ingredient().key(), stack.ingredient().amount());
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
        BigIngredientStack stack = (BigIngredientStack) instance;
        if (stack.ingredient().key() instanceof FluidIngredientKey fluidKey) {
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
    private GuiGameElement.GuiRenderBuilder renderIngredientEntry(ItemStack itemStack, @Local(argsOnly = true) BigItemStack entry, @Local(argsOnly = true) GuiGraphics graphics) {
        BigIngredientStack stack = (BigIngredientStack) entry;
        IngredientGui.renderSlot(graphics, stack.ingredient().key(), 0, 0);
        return GuiGameElement.of(Blocks.AIR);
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;drawItemCount(Lnet/minecraft/client/gui/GuiGraphics;II)V"
            )
    )
    private void renderIngredientEntryAmount(StockKeeperRequestScreen instance, GuiGraphics graphics, int count, int customCount,
                                             @Local(argsOnly = true) BigItemStack entry,
                                             @Local(argsOnly = true, ordinal = 0) boolean isStackHovered,
                                             @Local(argsOnly = true, ordinal = 1) boolean isRenderingOrders) {
        // todo workaround amount text rendering over tooltip for order entries
        if (isStackHovered && isRenderingOrders && !(entry instanceof CraftableBigItemStack)) return;
        BigIngredientStack stack = (BigIngredientStack) entry;
        count = customCount;
        IngredientGui.renderDecorations(graphics, stack.ingredient().withAmount(count), 1, 1);
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
            )
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
            )
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
            )
    )
    private void increaseOrderCountCraftable(BigItemStack instance, int count) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(count);
    }

    @Overwrite
    private void updateCraftableAmounts() {
        InventorySummary usedItems = new InventorySummary();
        InventorySummary availableItems = new InventorySummary();

        IngredientInventorySummary usedIngredients = (IngredientInventorySummary) usedItems;
        IngredientInventorySummary availableIngredients = (IngredientInventorySummary) availableItems;

        for (BigItemStack ordered : itemsToOrder) {
            BigIngredientStack orderedStack = (BigIngredientStack) ordered;
            availableIngredients.add(orderedStack.ingredient());
        }

        for (CraftableBigItemStack cbis : recipesToOrder) {
            CraftableIngredientStack craftableStack = (CraftableIngredientStack) cbis;
            if (craftableStack.ingredients().isEmpty()) {
                Pair<Integer, List<List<BigItemStack>>> craftingResult =
                        maxCraftable(cbis, availableItems, stack -> -usedItems.getCountOf(stack), -1);
                int maxCraftable = craftingResult.getFirst();
                List<List<BigItemStack>> validEntriesByIngredient = craftingResult.getSecond();
                int outputCount = craftableStack.outputCount(blockEntity.getLevel());

                // Only tweak amounts downward
                craftableStack.setCount(Math.min(craftableStack.getCount(), maxCraftable));

                // Use ingredients up before checking next recipe
                for (List<BigItemStack> list : validEntriesByIngredient) {
                    int remaining = cbis.count / outputCount;
                    for (BigItemStack entry : list) {
                        if (remaining <= 0)
                            break;
                        usedItems.add(entry.stack, Math.min(remaining, entry.count));
                        remaining -= entry.count;
                    }
                }
            } else {
                Pair<Integer, List<Pair<BoardIngredient, BoardIngredient>>> craftingResult =
                        createFactoryLogistics$maxCraftable(craftableStack, availableIngredients, stack -> -usedIngredients.getCountOf(stack.key()), -1);

                int outputCount = craftableStack.outputCount(blockEntity.getLevel());

                // Only tweak amounts downward
                craftableStack.setCount(Math.min(craftableStack.getCount(), craftingResult.getFirst()));

                // Use ingredients up before checking next recipe
                int remaining = cbis.count / outputCount;
                for (Pair<BoardIngredient, BoardIngredient> ingredient : craftingResult.getSecond()) {
                    if (remaining <= 0)
                        break;
                    int count = usedIngredients.getCountOf(ingredient.getSecond().key());
                    usedIngredients.add(ingredient.getSecond().withAmount(Math.min(remaining, count)));
                    remaining -= count;
                }
            }
        }

        canRequestCraftingPackage = false;
        for (BigItemStack ordered : itemsToOrder) {
            BigIngredientStack orderedStack = (BigIngredientStack) ordered;
            if (usedIngredients.getCountOf(orderedStack) != orderedStack.getCount())
                return;
        }
        canRequestCraftingPackage = true;
    }

    @WrapOperation(
            method = "sendIt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"
            )
    )
    private void sendRequest(NetworkHelper instance, CustomPacketPayload customPacketPayload, Operation<Void> original, @Local PackageOrderWithCrafts order) {
        ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket packet =
                new ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket(
                        blockEntity.getBlockPos(), IngredientOrder.of(order), addressBox.getValue(), encodeRequester);

        instance.sendToServer(packet);
    }

    @WrapMethod(
            method = "requestCraftable"
    )
    private void requestIngredients(CraftableBigItemStack cbis, int requestedDifference, Operation<Void> original) {
        CraftableIngredientStack stack = (CraftableIngredientStack) cbis;
        if (stack.ingredients().isEmpty()) {
            original.call(cbis, requestedDifference);
            return;
        }

        boolean takeOrdersAway = requestedDifference < 0;
        if (takeOrdersAway)
            requestedDifference = Math.max(-cbis.count, requestedDifference);
        if (requestedDifference == 0)
            return;

        IngredientInventorySummary availableItems = (IngredientInventorySummary) blockEntity.getLastClientsideStockSnapshotAsSummary();
        Function<BoardIngredient, Integer> countModifier = ingredient -> {
            BigIngredientStack ordered = createFactoryLogistics$getOrderForIngredient(ingredient);
            return ordered == null ? 0 : -ordered.getCount();
        };

        if (takeOrdersAway) {
            availableItems = (IngredientInventorySummary) new InventorySummary();
            for (BigItemStack ordered : itemsToOrder) {
                BigIngredientStack orderedStack = (BigIngredientStack) ordered;
                availableItems.add(orderedStack.ingredient());
            }
            countModifier = ingredient -> 0;
        }

        CraftableIngredientStack craftableStack = (CraftableIngredientStack) cbis;

        Pair<Integer, List<Pair<BoardIngredient, BoardIngredient>>> craftingResult =
                createFactoryLogistics$maxCraftable(craftableStack, availableItems, countModifier, takeOrdersAway ? -1 : 9 - itemsToOrder.size());
        int outputCount = craftableStack.outputCount(blockEntity.getLevel());
        int adjustToRecipeAmount = Mth.ceil(Math.abs(requestedDifference) / (float) outputCount) * outputCount;
        int maxCraftable = Math.min(adjustToRecipeAmount, craftingResult.getFirst());

        if (maxCraftable == 0)
            return;

        craftableStack.setCount(craftableStack.getCount() + (takeOrdersAway ? -maxCraftable : maxCraftable));

        List<Pair<BoardIngredient, BoardIngredient>> validEntriesByIngredient = craftingResult.getSecond();
        for (Pair<BoardIngredient, BoardIngredient> entry : validEntriesByIngredient) {
            int remaining = maxCraftable / outputCount;
            for (int i = 0; i < maxCraftable; i++) {
                if (remaining <= 0)
                    break;
                int toTransfer = Math.min(remaining, entry.getSecond().amount());
                BigIngredientStack order = createFactoryLogistics$getOrderForIngredient(entry.getSecond());

                if (takeOrdersAway) {
                    if (order != null) {
                        order.setCount(order.getCount() - toTransfer * entry.getFirst().amount());
                        if (order.getCount() <= 0)
                            itemsToOrder.remove(order);
                    }
                } else {
                    if (order == null) {
                        order = (BigIngredientStack) new BigItemStack(ItemStack.EMPTY, 0);
                        order.setIngredient(entry.getSecond().withAmount(0));
                        itemsToOrder.add(order.asStack());
                    }
                    order.setCount(order.getCount() + toTransfer * entry.getFirst().amount());
                }

                remaining -= 1;
            }
        }
    }

    @Unique
    private Pair<Integer, List<Pair<BoardIngredient, BoardIngredient>>> createFactoryLogistics$maxCraftable(CraftableIngredientStack cbis, IngredientInventorySummary summary, Function<BoardIngredient, Integer> countModifier, int newTypeLimit) {
        // original ingredient -> result with amount representing original times x
        List<Pair<BoardIngredient, BoardIngredient>> validIngredients = new ArrayList<>(cbis.ingredients().size());
        for (BoardIngredient ingredient : cbis.ingredients()) {
            BoardIngredient storedIngredient = ingredient.withAmount(summary.getCountOf(ingredient.key()));
            int storedAmount = storedIngredient.amount() + countModifier.apply(storedIngredient);
            validIngredients.add(Pair.of(ingredient, storedIngredient.withAmount(storedAmount / ingredient.amount())));
        }
        // Used new items may have to be trimmed
        if (newTypeLimit != -1) {
            int toRemove = (int) validIngredients.stream()
                    .filter(entry -> createFactoryLogistics$getOrderForIngredient(entry.getSecond()) == null)
                    .distinct()
                    .count() - newTypeLimit;

            validIngredients.sort(Comparator.comparingInt(p -> p.getSecond().amount()));
            for (int i = 0; i < toRemove; i++) {
                validIngredients.remove(validIngredients.size() - 1);
            }
        }

        // Determine the bottlenecking ingredient
        int minCount = Integer.MAX_VALUE;
        for (Pair<BoardIngredient, BoardIngredient> ingredient : validIngredients) {
            minCount = Math.min(ingredient.getSecond().amount() * ingredient.getFirst().amount(), minCount);
        }

        if (minCount == 0)
            return Pair.of(0, List.of());

        int outputCount = cbis.outputCount(blockEntity.getLevel());
        return Pair.of(minCount * outputCount, validIngredients);
    }
}
