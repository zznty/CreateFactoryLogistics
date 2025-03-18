package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterConfigurationPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.request.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu> {

    @Shadow(remap = false)
    private List<Integer> amounts;

    @Shadow(remap = false)
    private EditBox addressBox;

    public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;stacks()Ljava/util/List;"
            ),
            remap = false
    )
    private List<BigIngredientStack> getOrderAmounts(PackageOrderWithCrafts instance, Operation<List<BigItemStack>> original) {
        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) menu.contentHolder;
        return requester.getOrder().stacks();
    }

    @WrapOperation(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private ItemStack getIngredientInSlot(ItemStackHandler instance, int slot, Operation<ItemStack> original,
                                          @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;

        ingredient.set(ghostMenu.getIngredientInSlot(slot));

        return ItemStack.EMPTY;
    }

    @ModifyExpressionValue(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isIngredientEmpty(boolean original, @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        return ingredient.get() == BoardIngredient.EMPTY;
    }

    @WrapOperation(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void renderIngredient(GuiGraphics instance, Font l, ItemStack i, int x, int y, String i1, Operation<Void> original,
                                  @Local(index = 7) int index,
                                  @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        if (ingredient.get() instanceof FluidBoardIngredient fluidIngredient) {
            i = fluidIngredient.stack().getFluid().getBucket().getDefaultInstance();
            i1 = FactoryFluidPanelBehaviour.formatLevel(amounts.get(index)).string();

            GuiGameElement.of(fluidIngredient.stack().getFluid())
                    .scale(15)
                    .atLocal(1 / 32f, 1 + 1 / 32f, 2)
                    .render(instance, x, y);
        } else if (ingredient.get() instanceof ItemBoardIngredient itemIngredient) {
            i = itemIngredient.stack();
        }

        original.call(instance, l, i, x, y, i1);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack getIngredientInSlotMouseHandler(ItemStackHandler instance, int slot, Operation<ItemStack> original,
                                                      @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;

        ingredient.set(ghostMenu.getIngredientInSlot(slot));

        return ItemStack.EMPTY;
    }

    @ModifyExpressionValue(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isIngredientEmptyMouseHandler(boolean original, @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        return ingredient.get() == BoardIngredient.EMPTY;
    }

    @ModifyConstant(
            method = "mouseScrolled",
            constant = @Constant(intValue = 256)
    )
    private int modifyMaxAmount(int constant, @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        // 16 jars per fluid at max
        // I don't know why create limits this in the first place
        return ingredient.get() instanceof FluidBoardIngredient ? 16 * JarPackageItem.JAR_CAPACITY : constant;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
            IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
            BoardIngredient ingredient = ghostMenu.getIngredientInSlot(this.hoveredSlot.getSlotIndex());

            if (ingredient != BoardIngredient.EMPTY) {
                LangBuilder name;
                if (ingredient instanceof FluidBoardIngredient fluidIngredient) {
                    name = CreateLang.translate("gui.factory_panel.send_item", CreateLang.fluidName(fluidIngredient.stack())
                            .space()
                            .add(FactoryFluidPanelBehaviour.formatLevel(amounts.get(this.hoveredSlot.getSlotIndex()))));
                } else if (ingredient instanceof ItemBoardIngredient itemIngredient) {
                    name = CreateLang.translate("gui.factory_panel.send_item", CreateLang.itemName(itemIngredient.stack())
                            .add(CreateLang.text(" x" + amounts.get(this.hoveredSlot.getSlotIndex()))));
                } else {
                    return; // unreachable
                }

                List<Component> components = List.of(name
                                .color(ScrollInput.HEADER_RGB)
                                .component(),
                        CreateLang.translate("gui.factory_panel.scroll_to_change_amount")
                                .style(ChatFormatting.DARK_GRAY)
                                .style(ChatFormatting.ITALIC)
                                .component(),
                        CreateLang.translate("gui.scrollInput.shiftScrollsFaster")
                                .style(ChatFormatting.DARK_GRAY)
                                .style(ChatFormatting.ITALIC)
                                .component());

                graphics.renderTooltip(this.font, components, Optional.empty(), x, y);
                return;
            }

            super.renderTooltip(graphics, x, y);
        }
    }

    @WrapOperation(
            method = "getTooltipFromContainerItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/gui/menu/AbstractSimiContainerScreen;getTooltipFromContainerItem(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            )
    )
    private List<Component> getTooltipFromContainerItem(RedstoneRequesterScreen instance, ItemStack stack, Operation<List<Component>> original) {
        return stack.getItem() instanceof JarPackageItem ?
                List.of(FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).getDisplayName()) :
                original.call(instance, stack);
    }

    @Overwrite(remap = false)
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
        IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
        for (int i = 0; i < amounts.size(); i++)
            if (ghostMenu.getIngredientInSlot(i) == BoardIngredient.EMPTY)
                amounts.set(i, 1);
    }

    @Override
    protected void slotClicked(@Nullable Slot p_97778_, int p_97779_, int p_97780_, ClickType p_97781_) {
        if (p_97778_ != null && p_97778_ instanceof SlotItemHandler) {
            IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
            if (!this.menu.getCarried().isEmpty()) {
                FluidStack stack = FluidUtil.getFluidContained(this.menu.getCarried()).orElse(FluidStack.EMPTY);
                if (!stack.isEmpty() && hasAltDown()) {
                    ghostMenu.setIngredientInSlot(p_97778_.getSlotIndex(), new FluidBoardIngredient(stack).withAmount(1));
                    return;
                } else {
                    ghostMenu.setIngredientInSlot(p_97778_.getSlotIndex(), new ItemBoardIngredient(this.menu.getCarried()).withAmount(1));
                }
            } else {
                ghostMenu.setIngredientInSlot(p_97778_.getSlotIndex(), BoardIngredient.EMPTY);
            }
        }
        super.slotClicked(p_97778_, p_97779_, p_97780_, p_97781_);
    }

    @ModifyExpressionValue(
            method = "removed",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/core/BlockPos;Ljava/lang/String;ZLjava/util/List;)Lcom/simibubi/create/content/logistics/redstoneRequester/RedstoneRequesterConfigurationPacket;"
            )
    )
    private RedstoneRequesterConfigurationPacket createPacket(RedstoneRequesterConfigurationPacket original) {
        IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;

        List<BoardIngredient> ingredients = ghostMenu.getIngredients();

        List<BigIngredientStack> stacks = new ArrayList<>(ingredients.size());

        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i) == BoardIngredient.EMPTY) {
                continue;
            }
            stacks.add(BigIngredientStack.of(ingredients.get(i), amounts.get(i)));
        }

        IngredientRedstoneRequesterConfigurationPacket packet = (IngredientRedstoneRequesterConfigurationPacket) original;
        packet.setStacks(stacks);

        return original;
    }
}
