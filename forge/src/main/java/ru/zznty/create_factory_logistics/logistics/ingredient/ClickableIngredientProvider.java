package ru.zznty.create_factory_logistics.logistics.ingredient;

import net.createmod.catnip.data.Pair;
import net.minecraft.client.renderer.Rect2i;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

public interface ClickableIngredientProvider {
    Pair<GenericKey, Rect2i> getHoveredKey(int mouseX, int mouseY);
}
