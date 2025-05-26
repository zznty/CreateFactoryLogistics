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
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterConfigurationPacket;
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
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericGhostMenu;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequesterConfigurationPacket;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;

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
            GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;
            GenericStack stack = ghostMenu.getGenericSlot(p_282613_.getSlotIndex());
            GenericContentExtender.registrationOf(stack.key())
                    .clientProvider().guiHandler()
                    .renderSlot(p_281607_, stack.key(), p_282613_.x, p_282613_.y);
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
    private List<BigGenericStack> getOrderAmounts(PackageOrderWithCrafts instance,
                                                  Operation<List<BigItemStack>> original) {
        GenericRedstoneRequester requester = (GenericRedstoneRequester) menu.contentHolder;
        return requester.getOrder().stacks().stream().map(BigGenericStack::of).toList();
    }

    @WrapOperation(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack getIngredientInSlot(ItemStackHandler instance, int slot, Operation<ItemStack> original,
                                          @Share("genericStackLocalRef") LocalRef<GenericStack> genericStackLocalRef) {
        GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;

        genericStackLocalRef.set(ghostMenu.getGenericSlot(slot));

        return ItemStack.EMPTY;
    }

    @ModifyExpressionValue(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isIngredientEmpty(boolean original,
                                      @Share("genericStackLocalRef") LocalRef<GenericStack> genericStackLocalRef) {
        return genericStackLocalRef.get().isEmpty();
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
                                  @Share("genericStackLocalRef") LocalRef<GenericStack> genericStackLocalRef) {
        GenericContentExtender.registrationOf(genericStackLocalRef.get().key())
                .clientProvider().guiHandler()
                .renderDecorations(instance, genericStackLocalRef.get().key(), amounts.get(index), j, k);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack getIngredientInSlotMouseHandler(ItemStackHandler instance, int slot,
                                                      Operation<ItemStack> original,
                                                      @Share("genericStackLocalRef") LocalRef<GenericStack> genericStackLocalRef) {
        GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;

        genericStackLocalRef.set(ghostMenu.getGenericSlot(slot));

        return ItemStack.EMPTY;
    }

    @ModifyExpressionValue(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isIngredientEmptyMouseHandler(boolean original,
                                                  @Share("genericStackLocalRef") LocalRef<GenericStack> genericStackLocalRef) {
        return genericStackLocalRef.get().isEmpty();
    }

    @ModifyConstant(
            method = "mouseScrolled",
            constant = @Constant(intValue = 256)
    )
    private int modifyMaxAmount(int constant,
                                @Share("genericStackLocalRef") LocalRef<GenericStack> genericStackLocalRef) {
        // I don't know why create limits this in the first place
        return genericStackLocalRef.get().key() instanceof ItemKey ?
               constant :
               Integer.MAX_VALUE;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot instanceof SlotItemHandler) {
            GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;
            GenericStack stack = ghostMenu.getGenericSlot(this.hoveredSlot.getSlotIndex());

            if (!stack.isEmpty()) {
                LangBuilder name = CreateLang.translate("gui.factory_panel.send_item",
                                                        GenericContentExtender.registrationOf(stack.key())
                                                                .clientProvider().guiHandler().nameBuilder(
                                                                        stack.key(),
                                                                        amounts.get(this.hoveredSlot.getSlotIndex())));

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
    private List<Component> getTooltipFromContainerItem(RedstoneRequesterScreen instance, ItemStack itemStack,
                                                        Operation<List<Component>> original) {
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
        GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;
        for (int i = 0; i < amounts.size(); i++)
            if (ghostMenu.getGenericSlot(i).isEmpty())
                amounts.set(i, 1);
    }

    @Override
    protected void slotClicked(@Nullable Slot p_97778_, int p_97779_, int p_97780_, ClickType p_97781_) {
        if (p_97778_ instanceof SlotItemHandler) {
            GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;
            if (this.menu.getCarried().isEmpty()) {
                ghostMenu.setSlot(p_97778_.getSlotIndex(), GenericStack.EMPTY);
            } else {
                if (hasAltDown() && GenericItemEmptying.canItemBeEmptied(this.menu.contentHolder.getLevel(),
                                                                         this.menu.getCarried())) {
                    FluidStack stack = GenericItemEmptying.emptyItem(this.menu.contentHolder.getLevel(),
                                                                     this.menu.getCarried(), true).getFirst();
                    if (!stack.isEmpty())
                        ghostMenu.setSlot(p_97778_.getSlotIndex(), FluidGenericStack.wrap(stack).withAmount(1));
                    return;
                }

                ghostMenu.setSlot(p_97778_.getSlotIndex(),
                                  GenericStack.wrap(this.menu.getCarried().copy()).withAmount(1));
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
        GenericGhostMenu ghostMenu = (GenericGhostMenu) menu;

        List<GenericStack> genericStack = ghostMenu.getStacks();

        List<GenericStack> stacks = new ArrayList<>(genericStack.size());

        for (int i = 0; i < genericStack.size(); i++) {
            if (genericStack.get(i).isEmpty()) {
                continue;
            }
            stacks.add(genericStack.get(i).withAmount(amounts.get(i)));
        }

        RedstoneRequesterConfigurationPacket packet = new RedstoneRequesterConfigurationPacket(menu.contentHolder.getBlockPos(),
                                                                                               addressBox.getValue(), allowPartial.green, stacks);

        instance.sendToServer(packet);
    }
}
