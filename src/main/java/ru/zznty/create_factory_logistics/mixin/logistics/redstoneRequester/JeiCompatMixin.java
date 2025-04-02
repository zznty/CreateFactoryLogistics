package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientGhostMenu;

@Mixin(targets = "com/simibubi/create/compat/jei/GhostIngredientHandler$GhostTarget")
public class JeiCompatMixin {
    @Final
    @Shadow(remap = false)
    private AbstractSimiContainerScreen gui;

    @WrapOperation(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemStackHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"
            ),
            remap = false
    )
    private void setInSlot(ItemStackHandler instance, int slot, ItemStack stack, Operation<Void> original) {
        if (gui.getMenu() instanceof IngredientGhostMenu ingredientGhostMenu) {
            IngredientKey key;
            if (GenericItemEmptying.canItemBeEmptied(Minecraft.getInstance().level, stack) && Screen.hasAltDown()) {
                key = IngredientKey.of(GenericItemEmptying.emptyItem(Minecraft.getInstance().level, stack, true).getFirst());
            } else {
                key = IngredientKey.of(stack);
            }
            ingredientGhostMenu.setIngredientInSlot(slot, new BoardIngredient(key, 1));
            return;
        }

        original.call(instance, slot, stack);
    }
}
