package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.logistics.ingredient.*;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientGhostMenu;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRedstoneRequester;
import ru.zznty.create_factory_logistics.logistics.panel.request.RedstoneRequesterConfigurationPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu> {

    @Shadow
    private List<Integer> amounts;

    @Shadow
    private AddressEditBox addressBox;

    @Shadow
    private IconButton allowPartial;

    public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    public void renderSlot(GuiGraphics p_281607_, Slot p_282613_) {
        if (p_282613_ instanceof SlotItemHandler) {
            IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
            BoardIngredient ingredient = ghostMenu.getIngredientInSlot(p_282613_.getSlotIndex());
            IngredientGui.renderSlot(p_281607_, ingredient.key(), p_282613_.x, p_282613_.y);
            return;
        }

        super.renderSlot(p_281607_, p_282613_);
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;stacks()Ljava/util/List;"
            )
    )
    private List<BigIngredientStack> getOrderAmounts(PackageOrderWithCrafts instance, Operation<List<BigItemStack>> original) {
        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) menu.contentHolder;
        return requester.getOrder().stacks();
    }

    @WrapOperation(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            )
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
        return ingredient.get().isEmpty();
    }

    @Redirect(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void renderIngredient(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1,
                                  @Local(index = 7) int index,
                                  @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        IngredientGui.renderDecorations(instance, ingredient.get().withAmount(amounts.get(index)), j, k);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
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
        return ingredient.get().isEmpty();
    }

    @ModifyConstant(
            method = "mouseScrolled",
            constant = @Constant(intValue = 256)
    )
    private int modifyMaxAmount(int constant, @Share("ingredient") LocalRef<BoardIngredient> ingredient) {
        // 16 jars per fluid at max
        // I don't know why create limits this in the first place
        return ingredient.get().key().provider() == IngredientProviders.FLUID.get() ? 16 * Config.jarCapacity : constant;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot instanceof SlotItemHandler) {
            IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
            BoardIngredient ingredient = ghostMenu.getIngredientInSlot(this.hoveredSlot.getSlotIndex());

            if (!ingredient.isEmpty()) {
                LangBuilder name = CreateLang.translate("gui.factory_panel.send_item", IngredientGui.nameBuilder(ingredient.withAmount(amounts.get(this.hoveredSlot.getSlotIndex()))));

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

            if (menu.getCarried().isEmpty()) {
                List<Component> components = List.of(
                        Component.translatable("create_factory_logistics.gui.redstone_requester.fluid_slot_mode")
                                .withStyle(ChatFormatting.GRAY)
                );
                graphics.renderTooltip(this.font, components, Optional.empty(), x, y);
                return;
            }
        }

        super.renderTooltip(graphics, x, y);
    }

    @WrapOperation(
            method = "getTooltipFromContainerItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/gui/menu/AbstractSimiContainerScreen;getTooltipFromContainerItem(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            )
    )
    private List<Component> getTooltipFromContainerItem(RedstoneRequesterScreen instance, ItemStack itemStack, Operation<List<Component>> original) {
        if (!(hoveredSlot instanceof SlotItemHandler))
            return original.call(instance, itemStack);

        /*return stack.getItem() instanceof JarPackageItem ?
                List.of(FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).getDisplayName()) :
                original.call(instance, stack);*/
        return List.of();
    }

    @Overwrite
    protected void containerTick() {
        super.containerTick();
        addressBox.tick();
        IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
        for (int i = 0; i < amounts.size(); i++)
            if (ghostMenu.getIngredientInSlot(i).isEmpty())
                amounts.set(i, 1);
    }

    @Override
    protected void slotClicked(@Nullable Slot p_97778_, int p_97779_, int p_97780_, ClickType p_97781_) {
        if (p_97778_ instanceof SlotItemHandler) {
            IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;
            if (this.menu.getCarried().isEmpty()) {
                ghostMenu.setIngredientInSlot(p_97778_.getSlotIndex(), BoardIngredient.of());
            } else {
                if (hasAltDown() && GenericItemEmptying.canItemBeEmptied(this.menu.contentHolder.getLevel(), this.menu.getCarried())) {
                    FluidStack stack = GenericItemEmptying.emptyItem(this.menu.contentHolder.getLevel(), this.menu.getCarried(), true).getFirst();
                    if (!stack.isEmpty())
                        ghostMenu.setIngredientInSlot(p_97778_.getSlotIndex(), new BoardIngredient(IngredientKey.of(stack), 1));
                    return;
                }

                ghostMenu.setIngredientInSlot(p_97778_.getSlotIndex(), new BoardIngredient(IngredientKey.of(this.menu.getCarried().copy()), 1));
            }
        }
        super.slotClicked(p_97778_, p_97779_, p_97780_, p_97781_);
    }

    @WrapOperation(
            method = "removed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"
            )
    )
    private void createPacket(NetworkHelper instance, CustomPacketPayload customPacketPayload, Operation<Void> original) {
        IngredientGhostMenu ghostMenu = (IngredientGhostMenu) menu;

        List<BoardIngredient> ingredients = ghostMenu.getIngredients();

        List<BigIngredientStack> stacks = new ArrayList<>(ingredients.size());

        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).isEmpty()) {
                continue;
            }
            stacks.add(BigIngredientStack.of(ingredients.get(i), amounts.get(i)));
        }

        RedstoneRequesterConfigurationPacket packet = new RedstoneRequesterConfigurationPacket(menu.contentHolder.getBlockPos(),
                addressBox.getValue(), allowPartial.green, stacks);

        instance.sendToServer(packet);
    }
}
