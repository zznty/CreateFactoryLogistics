package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

import java.util.ArrayList;
import java.util.List;

public final class GenericTooltipHelper {

    private GenericTooltipHelper() {
    }

    public static List<Component> buildTooltip(GenericStack stack, int mouseX, int mouseY) {
        GenericKeyRegistration registration = GenericContentExtender.registrationOf(stack.key());
        List<Component> tooltip = new ArrayList<>(
                registration.clientProvider().guiHandler().tooltipBuilder(stack.key(), stack.amount()));

        NeoForge.EVENT_BUS.post(new GenericTooltipEvent(List.of(stack), tooltip, mouseX, mouseY));
        return tooltip;
    }

    public static List<Component> buildTooltip(GenericStack stack) {
        return buildTooltip(stack, 0, 0);
    }

    public static List<Component> buildTooltip(List<Component> customTooltip, GenericStack stack, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>(customTooltip);
        NeoForge.EVENT_BUS.post(new GenericTooltipEvent(List.of(stack), tooltip, mouseX, mouseY));
        return tooltip;
    }

    public static void fireTooltip(List<GenericStack> stacks, List<Component> tooltip) {
        NeoForge.EVENT_BUS.post(new GenericTooltipEvent(stacks, tooltip, 0, 0));
    }
}
