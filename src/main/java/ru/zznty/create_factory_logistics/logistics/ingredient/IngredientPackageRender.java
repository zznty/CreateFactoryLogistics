package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.EmptyPackageRenderHandler;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid.FluidPackageRenderHandler;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemPackageRenderHandler;

import java.util.IdentityHashMap;
import java.util.Map;

public final class IngredientPackageRender {
    @SuppressWarnings("rawtypes")
    public static final Map<IngredientKeyProvider, IngredientPackageRenderHandler> HANDLERS = new IdentityHashMap<>();

    static {
        HANDLERS.put(IngredientProviders.EMPTY.get(), new EmptyPackageRenderHandler());
        HANDLERS.put(IngredientProviders.ITEM.get(), new ItemPackageRenderHandler());
        HANDLERS.put(IngredientProviders.FLUID.get(), new FluidPackageRenderHandler());
    }

    public static void renderPanelFilter(IngredientKey filter, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        //noinspection unchecked
        HANDLERS.get(filter.provider()).renderPanelFilter(filter, ms, buffer, light, overlay);
    }
}
