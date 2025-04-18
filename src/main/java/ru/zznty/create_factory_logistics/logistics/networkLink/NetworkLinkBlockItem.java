package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry;

import java.util.List;
import java.util.UUID;

public class NetworkLinkBlockItem extends LogisticallyLinkedBlockItem {
    public NetworkLinkBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return isTuned(pStack);
    }

    public static boolean isTuned(ItemStack pStack) {
        if (!pStack.hasTag()) return false;
        CompoundTag tag = pStack.getTagElement(BLOCK_ENTITY_TAG);
        return tag != null && tag.contains("Freq");
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        CompoundTag tag = pStack.getTagElement(BLOCK_ENTITY_TAG);
        if (tag == null || !tag.contains(NetworkLinkBlock.INGREDIENT_TYPE, CompoundTag.TAG_STRING))
            return;

        ResourceLocation ingredientType = ResourceLocation.parse(tag.getString(NetworkLinkBlock.INGREDIENT_TYPE));

        if (ingredientType.getPath().equals(IngredientRegistry.EMPTY_KEY))
            return;

        CreateLang.builder(ingredientType.getNamespace())
                .add(CreateLang.builder(CreateFactoryLogistics.MODID).translate("gui.ingredient_type_qualified_item"))
                .space()
                .translate("gui.ingredient_type." + ingredientType.getPath())
                .style(ChatFormatting.ITALIC)
                .addTo(pTooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return super.useOn(pContext);

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return InteractionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return InteractionResult.SUCCESS;
        }

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide || useOn == InteractionResult.FAIL)
            return useOn;

        player.displayClientMessage(tuned ? CreateLang.translateDirect("logistically_linked.connected")
                : CreateLang.translateDirect("logistically_linked.new_network_started"), true);
        return useOn;
    }

    // thank create for static method
    public static void assignFrequency(ItemStack stack, Player player, UUID frequency) {
        CompoundTag teTag = stack.getOrCreateTagElement(BLOCK_ENTITY_TAG);
        teTag.putUUID("Freq", frequency);

        player.displayClientMessage(CreateLang.translateDirect("logistically_linked.tuned"), true);
    }
}
