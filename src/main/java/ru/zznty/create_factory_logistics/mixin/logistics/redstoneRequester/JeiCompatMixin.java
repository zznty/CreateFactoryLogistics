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
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientGhostMenu;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;

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
            BoardIngredient ingredient;
            if (GenericItemEmptying.canItemBeEmptied(Minecraft.getInstance().level, stack) && Screen.hasAltDown()) {
                ingredient = new FluidBoardIngredient(GenericItemEmptying.emptyItem(Minecraft.getInstance().level, stack, true).getFirst(), 1);
            } else {
                ingredient = new ItemBoardIngredient(stack, 1);
            }
            ingredientGhostMenu.setIngredientInSlot(slot, ingredient);
            return;
        }

        original.call(instance, slot, stack);
    }
}
