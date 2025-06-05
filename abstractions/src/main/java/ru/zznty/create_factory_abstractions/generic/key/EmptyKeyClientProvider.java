package ru.zznty.create_factory_abstractions.generic.key;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientGuiHandler;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyClientRenderHandler;

import java.util.List;

public class EmptyKeyClientProvider implements GenericKeyClientProvider<EmptyKey> {
    @Override
    public GenericKeyClientGuiHandler<EmptyKey> guiHandler() {
        return new GenericKeyClientGuiHandler<>() {
            @Override
            public void renderDecorations(GuiGraphics graphics, EmptyKey key, int amount, int x, int y) {
            }

            @Override
            public void renderSlot(GuiGraphics graphics, EmptyKey key, int x, int y) {
            }

            @Override
            public LangBuilder nameBuilder(EmptyKey key, int amount) {
                return CreateLang.text("");
            }

            @Override
            public List<Component> tooltipBuilder(EmptyKey key, int amount) {
                return List.of();
            }

            @Override
            public LangBuilder nameBuilder(EmptyKey key) {
                return CreateLang.text("");
            }

            @Override
            public int stackSize(EmptyKey key) {
                return Item.DEFAULT_MAX_STACK_SIZE;
            }

            @Override
            public int maxStackSize(EmptyKey key) {
                return Item.DEFAULT_MAX_STACK_SIZE;
            }
        };
    }

    @Override
    public GenericKeyClientRenderHandler<EmptyKey> renderHandler() {
        return (key, ms, buffer, light, overlay) -> {
        };
    }
}
