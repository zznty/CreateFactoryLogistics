package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_abstractions.api.generic.crafting.OrderProvider;
import ru.zznty.create_factory_abstractions.api.generic.crafting.RecipeRequestHelper;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.search.CategoriesProvider;
import ru.zznty.create_factory_abstractions.api.generic.search.GenericSearch;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_logistics.logistics.ingredient.ClickableIngredientProvider;
import ru.zznty.create_factory_logistics.mixin.accessor.CategoryEntryAccessor;
import ru.zznty.create_factory_logistics.mixin.accessor.StockTickerBlockEntityAccessor;

import java.util.*;
import java.util.function.Function;

@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> implements OrderProvider, CategoriesProvider, ClickableIngredientProvider {
    public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Shadow(remap = false)
    private InventorySummary forcedEntries;

    @Shadow(remap = false)
    public List<BigGenericStack> itemsToOrder;

    @Shadow(remap = false)
    StockTickerBlockEntity blockEntity;

    @Shadow(remap = false)
    public List<List<BigGenericStack>> currentItemSource;

    @Shadow(remap = false)
    public List<CraftableGenericStack> recipesToOrder;

    @Shadow(remap = false)
    private boolean canRequestCraftingPackage;

    @Shadow
    int orderY;

    @Shadow(remap = false)
    public LerpedFloat itemScroll;

    @Shadow(remap = false)
    public List<List<BigGenericStack>> displayedItems;

    @Shadow(remap = false)
    public EditBox searchBox;

    @Final
    @Shadow(remap = false)
    int cols, rows;

    @Final
    @Shadow(remap = false)
    int rowHeight, colWidth;

    @Final
    @Shadow(remap = false)
    private Set<Integer> hiddenCategories;

    @Shadow(remap = false)
    private Pair<Integer, List<List<BigItemStack>>> maxCraftable(CraftableBigItemStack cbis, InventorySummary summary,
                                                                 Function<ItemStack, Integer> countModifier,
                                                                 int newTypeLimit) {
        return null;
    }

    @Shadow(remap = false)
    private void clampScrollBar() {
    }

    @Shadow(remap = false)
    public boolean isSchematicListMode() {
        return false;
    }

    @Shadow(remap = false)
    public void requestSchematicList() {
    }

    @Shadow
    public List<StockKeeperRequestScreen.CategoryEntry> categories;

    @Final
    @Shadow(remap = false)
    Couple<Integer> noneHovered;

    @Shadow(remap = false)
    int itemsX, itemsY;

    @Shadow(remap = false)
    int windowWidth;

    @Shadow(remap = false)
    private Couple<Integer> getHoveredSlot(int x, int y) {
        return noneHovered;
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
            ),
            remap = false
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

    @Overwrite(remap = false)
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
                    target = "Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"
            )
    )
    private List<Component> getCraftableTooltip(ItemStack instance, Player player, TooltipFlag flag,
                                                @Local BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);
        return GenericContentExtender.registrationOf(
                stack.get().key()).clientProvider().guiHandler().tooltipBuilder(stack.get().key(),
                                                                                stack.get().amount());
    }

    @Overwrite(remap = false)
    private void refreshSearchResults(boolean scrollBackUp) {
        displayedItems = Collections.emptyList();
        if (scrollBackUp)
            itemScroll.startWithValue(0);

        if (currentItemSource == null) {
            clampScrollBar();
            return;
        }

        if (isSchematicListMode()) {
            clampScrollBar();
            requestSchematicList();
            return;
        }

        GenericSearch.SearchResult result = GenericSearch.search(this, searchBox.getValue(), rowHeight, cols);

        displayedItems = result.displayedItems();
        categories = new ArrayList<>(result.categories().size());

        for (int i = 0; i < result.categories().size(); i++) {
            GenericSearch.CategoryEntry entry = result.categories().get(i);
            StockKeeperRequestScreen.CategoryEntry categoryEntry = new StockKeeperRequestScreen.CategoryEntry(
                    entry.targetCategory(), entry.name(), entry.y().getValue());
            ((CategoryEntryAccessor) categoryEntry).setHidden(entry.hidden().getValue());
            categories.add(categoryEntry);
        }

        clampScrollBar();
        updateCraftableAmounts();
    }

    @Redirect(
            method = "renderItemEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/gui/element/GuiGameElement;of(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;"
            ),
            remap = false
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
            ),
            remap = false
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

    @Overwrite(remap = false)
    private void updateCraftableAmounts() {
        canRequestCraftingPackage = RecipeRequestHelper.updateCraftableAmounts(this);
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

    @Override
    public List<ItemStack> categories() {
        return ((StockTickerBlockEntityAccessor) blockEntity).getCategories();
    }

    @Override
    public Set<Integer> hiddenCategories() {
        return hiddenCategories;
    }

    @Override
    public List<List<BigGenericStack>> currentItemSource() {
        return currentItemSource;
    }

    @Override
    public Pair<GenericKey, Rect2i> getHoveredKey(int mouseX, int mouseY) {
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

        if (hoveredSlot != noneHovered) {
            int index = hoveredSlot.getSecond();
            boolean recipeHovered = hoveredSlot.getFirst() == -2;
            boolean orderHovered = hoveredSlot.getFirst() == -1;

            int x, y;
            BigGenericStack entry;
            if (recipeHovered) {
                int jeiX = getGuiLeft() + (windowWidth - colWidth * recipesToOrder.size()) / 2 + 1;
                int jeiY = orderY - 31;

                x = jeiX + (index * colWidth);
                y = jeiY;

                entry = recipesToOrder.get(index);
            } else {
                if (orderHovered) {
                    x = itemsX + index * colWidth;
                    y = orderY;

                    entry = itemsToOrder.get(index);
                } else {
                    int categoryIndex = hoveredSlot.getFirst();
                    int categoryY = categories.isEmpty() ?
                                    0 :
                                    ((CategoryEntryAccessor) categories.get(categoryIndex)).getY();

                    x = itemsX + (index % cols) * colWidth;
                    y = itemsY + categoryY + (categories.isEmpty() ? 4 : rowHeight) + (index / cols) * rowHeight;

                    entry = displayedItems.get(categoryIndex).get(index);
                }
            }

            Rect2i bounds = new Rect2i(x, y, x + 18, y + 18);
            return Pair.of(entry.get().key(), bounds);
        }

        return Pair.of(GenericKey.EMPTY, new Rect2i(0, 0, 0, 0));
    }
}
