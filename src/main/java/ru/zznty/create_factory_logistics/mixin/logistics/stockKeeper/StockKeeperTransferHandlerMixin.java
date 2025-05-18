package ru.zznty.create_factory_logistics.mixin.logistics.stockKeeper;

import com.simibubi.create.compat.jei.StockKeeperTransferHandler;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.zznty.create_factory_logistics.compat.jei.IngredientTransfer;
import ru.zznty.create_factory_logistics.compat.jei.TransferOperation;
import ru.zznty.create_factory_logistics.compat.jei.TransferOperationsResult;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.CraftableIngredientStack;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Mixin(StockKeeperTransferHandler.class)
public class StockKeeperTransferHandlerMixin {
    @Shadow(remap = false)
    private IJeiHelpers helpers;

    @Overwrite(remap = false)
    private @Nullable IRecipeTransferError transferRecipeOnClient(StockKeeperRequestMenu container, Recipe<?> recipe,
                                                                  IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (!(container.screenReference instanceof StockKeeperRequestScreen screen))
            return null;

        for (CraftableBigItemStack cbis : screen.recipesToOrder)
            if (cbis.recipe == recipe)
                return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.already_ordering_recipe")
                        .component());

        if (screen.itemsToOrder.size() >= 9)
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                    .component());

        IngredientInventorySummary summary = (IngredientInventorySummary) screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
        if (summary == null)
            return null;

        List<BoardIngredient> availableStacks = summary.get();
        Container outputDummy = new RecipeWrapper(new ItemStackHandler(9));
        List<Slot> craftingSlots = new ArrayList<>();
        for (int i = 0; i < outputDummy.getContainerSize(); i++)
            craftingSlots.add(new Slot(outputDummy, i, 0, 0));

        TransferOperationsResult transferOperations = IngredientTransfer.getRecipeTransferOperations(helpers.getIngredientManager(),
                availableStacks, recipeSlots.getSlotViews(RecipeIngredientRole.INPUT), craftingSlots);

        if (!transferOperations.missingItems().isEmpty())
            return new RecipeTransferErrorMissingSlots(CreateLang.translate("gui.stock_keeper.not_in_stock")
                    .component(), transferOperations.missingItems());

        if (screen.itemsToOrder.size() + transferOperations.results().size() >= 9)
            return new RecipeTransferErrorTooltip(CreateLang.translate("gui.stock_keeper.slots_full")
                    .component());

        if (!doTransfer)
            return null;

        CraftableBigItemStack cbis = new CraftableBigItemStack(recipe.getResultItem(player.level()
                .registryAccess()), recipe);

        {
            CraftableIngredientStack ingredientStack = (CraftableIngredientStack) cbis;
            
            ingredientStack.setCount(0);

            for (TransferOperation operation : transferOperations.results()) {
                IIngredientHelper helper = helpers.getIngredientManager().getIngredientHelper(operation.selectedIngredient().getType());
                ingredientStack.ingredients().add(availableStacks.get(operation.from()).withAmount((int) helper.getAmount(operation.selectedIngredient().getIngredient())));
            }
        }

        if (cbis.stack.isEmpty()) {
            //noinspection DataFlowIssue
            BigIngredientStack stack = (BigIngredientStack) cbis;

            Outer:
            for (IRecipeSlotView view : recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT)) {
                Iterator<ITypedIngredient<?>> iterator = view.getAllIngredients().iterator();

                while (iterator.hasNext()) {
                    Optional<BoardIngredient> convertedStack = IngredientTransfer.tryConvert(helpers.getIngredientManager(), iterator.next());
                    if (convertedStack.isPresent()) {
                        stack.setIngredient(convertedStack.get());
                        break Outer;
                    }
                }
            }
        }

        screen.recipesToOrder.add(cbis);
        screen.searchBox.setValue("");
        screen.refreshSearchNextTick = true;
        screen.requestCraftable(cbis, maxTransfer && !cbis.stack.isEmpty() ? cbis.stack.getMaxStackSize() : 1);

        return null;
    }
}
