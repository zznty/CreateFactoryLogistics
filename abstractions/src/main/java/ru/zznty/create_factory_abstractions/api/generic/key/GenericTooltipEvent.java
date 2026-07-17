/**
 * Posted on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} whenever a generic tooltip
 * is built. Fires from:
 * <ul>
 *     <li>Stock keeper screen — item/fluid/chemical slot tooltips</li>
 *     <li>Redstone requester screen — ghost slot tooltips</li>
 *     <li>Package item {@code appendHoverText} — content tooltips for packages, jars, barrels, composites</li>
 * </ul>
 * <p>
 * Subscribing mods should call {@link #getTooltip()} and append their own
 * {@link net.minecraft.network.chat.Component} entries.
 * <p>
 * {@link #getStacks()} returns all generic stacks associated with this tooltip — a single
 * element for slot tooltips, or all visible content items for package tooltips.
 * <p>
 * This event is <b>not</b> cancellable.
 */
package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public class GenericTooltipEvent extends Event {

    private final List<GenericStack> stacks;
    private final List<Component> tooltip;
    private final int mouseX;
    private final int mouseY;

    public GenericTooltipEvent(List<GenericStack> stacks, List<Component> tooltip, int mouseX, int mouseY) {
        this.stacks = stacks;
        this.tooltip = tooltip;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    /**
     * @return the generic stacks associated with this tooltip context
     */
    public List<GenericStack> getStacks() {
        return stacks;
    }

    /**
     * @return mutable list of tooltip components — append new entries here
     */
    public List<Component> getTooltip() {
        return tooltip;
    }

    /** @return the mouse X position when the tooltip was rendered, or 0 if not in a screen context */
    public int getMouseX() {
        return mouseX;
    }

    /** @return the mouse Y position when the tooltip was rendered, or 0 if not in a screen context */
    public int getMouseY() {
        return mouseY;
    }
}
