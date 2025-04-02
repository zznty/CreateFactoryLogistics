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

public final class IngredientGui {
    @SuppressWarnings("rawtypes")
    public static final Map<IngredientKeyProvider, IngredientGuiHandler> HANDLERS = new IdentityHashMap<>();

    static {
        HANDLERS.put(IngredientProviders.EMPTY.get(), new EmptyGuiHandler());
        HANDLERS.put(IngredientProviders.ITEM.get(), new ItemGuiHandler());
        HANDLERS.put(IngredientProviders.FLUID.get(), new FluidGuiHandler());
    }

    public static void renderDecorations(GuiGraphics graphics, BoardIngredient ingredient, int x, int y) {
        //noinspection unchecked
        HANDLERS.get(ingredient.key().provider()).renderDecorations(graphics, ingredient.key(), ingredient.amount(), x, y);
    }

    public static LangBuilder nameBuilder(BoardIngredient ingredient) {
        //noinspection unchecked
        return HANDLERS.get(ingredient.key().provider()).nameBuilder(ingredient.key(), ingredient.amount());
    }

    public static List<Component> tooltipBuilder(IngredientKey key, int amount) {
        //noinspection unchecked
        return HANDLERS.get(key.provider()).tooltipBuilder(key, amount);
    }

    public static LangBuilder nameBuilder(IngredientKey key) {
        //noinspection unchecked
        return HANDLERS.get(key.provider()).nameBuilder(key);
    }

    public static int stackSize(IngredientKey key) {
        //noinspection unchecked
        return HANDLERS.get(key.provider()).stackSize(key);
    }

    public static int maxStackSize(IngredientKey key) {
        //noinspection unchecked
        return HANDLERS.get(key.provider()).maxStackSize(key);
    }

    public static void renderSlot(GuiGraphics graphics, IngredientKey key, int x, int y) {
        //noinspection unchecked
        HANDLERS.get(key.provider()).renderSlot(graphics, key, x, y);
    }
}
