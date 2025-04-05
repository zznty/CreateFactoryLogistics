package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.EmptyGuiHandler;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidGuiHandler;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemGuiHandler;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class IngredientGui {
    @SuppressWarnings("rawtypes")
    public static final Map<IngredientKeyProvider, IngredientGuiHandler> HANDLERS = new IdentityHashMap<>();

    static {
        HANDLERS.put(IngredientProviders.EMPTY.get(), new EmptyGuiHandler());
        HANDLERS.put(IngredientProviders.ITEM.get(), new ItemGuiHandler());
        HANDLERS.put(IngredientProviders.FLUID.get(), new FluidGuiHandler());
    }

    public static void renderDecorations(GuiGraphics graphics, BoardIngredient ingredient, int x, int y) {
        getUnchecked(ingredient.key()).renderDecorations(graphics, ingredient.key(), ingredient.amount(), x, y);
    }

    public static LangBuilder nameBuilder(BoardIngredient ingredient) {
        return getUnchecked(ingredient.key()).nameBuilder(ingredient.key(), ingredient.amount());
    }

    public static List<Component> tooltipBuilder(IngredientKey key, int amount) {
        return getUnchecked(key).tooltipBuilder(key, amount);
    }

    public static LangBuilder nameBuilder(IngredientKey key) {
        return getUnchecked(key).nameBuilder(key);
    }

    public static int stackSize(IngredientKey key) {
        return getUnchecked(key).stackSize(key);
    }

    public static int maxStackSize(IngredientKey key) {
        return getUnchecked(key).maxStackSize(key);
    }

    public static void renderSlot(GuiGraphics graphics, IngredientKey key, int x, int y) {
        getUnchecked(key).renderSlot(graphics, key, x, y);
    }

    @SuppressWarnings("rawtypes")
    private static IngredientGuiHandler getUnchecked(IngredientKey key) {
        return HANDLERS.get(key.provider());
    }
}
