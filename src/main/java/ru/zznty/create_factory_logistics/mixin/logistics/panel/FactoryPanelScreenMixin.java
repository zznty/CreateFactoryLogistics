package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientGui;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
    @Shadow
    private FactoryPanelBehaviour behaviour;

    @Shadow
    private BigItemStack outputConfig;

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/box/PackageStyles;getDefaultBox()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack promisePackageItem(Operation<ItemStack> original) {
        // todo provider for package model
        return behaviour instanceof FactoryFluidPanelBehaviour ? JarPackageItem.getDefaultJar() : original.call();
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/AllBlocks;FACTORY_GAUGE:Lcom/tterrag/registrate/util/entry/BlockEntry;"
            )
    )
    private BlockEntry<? extends FactoryPanelBlock> gaugeItem(Operation<BlockEntry<FactoryPanelBlock>> original) {
        // todo provider for block model
        return behaviour instanceof FactoryFluidPanelBehaviour ? FactoryBlocks.FACTORY_FLUID_GAUGE : original.call();
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/gui/element/GuiGameElement;of(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;",
                    ordinal = 1
            )
    )
    private GuiGameElement.GuiRenderBuilder filterItem(ItemStack stack, Operation<GuiGameElement.GuiRenderBuilder> original) {
        // todo provider for block filter render
        return behaviour instanceof FactoryFluidPanelBehaviour ?
                GuiGameElement.of(Blocks.AIR) :
                original.call(stack);
    }

    @Redirect(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V",
                    ordinal = 0
            )
    )
    private void renderOutputConfig(GuiGraphics instance, ItemStack p_281978_, int p_282647_, int p_281944_) {
        BoardIngredient ingredient = BoardIngredient.of(behaviour);

        IngredientGui.renderSlot(instance, ingredient.key(), p_282647_, p_281944_);
    }

    /*@Definition(id = "behaviour", field = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelScreen;behaviour:Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;")
    @Definition(id = "getFilter", method = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;getFilter()Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "getHoverName", method = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;")
    @Definition(id = "getString", method = "Lnet/minecraft/network/chat/Component;getString()Ljava/lang/String;")
    @Definition(id = "text", method = "Lcom/simibubi/create/foundation/utility/CreateLang;text(Ljava/lang/String;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Expression("text(this.behaviour.getFilter().getHoverName().getString() + ' x' + ?)")
    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )*/
    @Redirect(
            method = "renderWindow",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=gui.factory_panel.left_click_reset",
                    shift = At.Shift.BY, by = -4
            )
    )
    private LangBuilder promiseTipValueFormat(String text) {
        BoardIngredient ingredient = BoardIngredient.of(behaviour);

        return IngredientGui.nameBuilder(ingredient.withAmount(behaviour.getPromised()));
    }

    @Definition(id = "itemName", method = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Definition(id = "add", method = "Lnet/createmod/catnip/lang/LangBuilder;add(Lnet/createmod/catnip/lang/LangBuilder;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Expression("itemName(?).add(?)")
    @Redirect(
            method = "renderWindow",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private LangBuilder outputConfigTipFormat(LangBuilder instance, LangBuilder otherBuilder) {
        BoardIngredient ingredient = BoardIngredient.of(behaviour);

        return IngredientGui.nameBuilder(ingredient.withAmount(outputConfig.count));
    }

    @Redirect(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    ordinal = 1
            )
    )
    private void renderPromised(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1) {
        BoardIngredient ingredient = BoardIngredient.of(behaviour);

        IngredientGui.renderDecorations(instance, ingredient.withAmount(behaviour.getPromised()), j, k);
    }

    @Redirect(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    ordinal = 0
            )
    )
    private void outputConfigAmount(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1) {
        BigIngredientStack stack = (BigIngredientStack) outputConfig;

        IngredientGui.renderDecorations(instance, stack.ingredient(), j, k);
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;",
                    ordinal = 0
            )
    )
    private LangBuilder inputConfigRestockerTipFormat(ItemStack $, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        return IngredientGui.nameBuilder(stack.ingredient().key());
    }

    @Definition(id = "itemName", method = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Definition(id = "add", method = "Lnet/createmod/catnip/lang/LangBuilder;add(Lnet/createmod/catnip/lang/LangBuilder;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Expression("itemName(?).add(?)")
    @Redirect(
            method = "renderInputItem",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private LangBuilder inputConfigTipFormat(LangBuilder instance, LangBuilder otherBuilder, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        return IngredientGui.nameBuilder(stack.ingredient());
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void inputConfigAmount(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        IngredientGui.renderDecorations(instance, stack.ingredient(), j, k);
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"
            )
    )
    private void renderInputFluid(GuiGraphics instance, ItemStack p_281978_, int p_282647_, int p_281944_, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        IngredientGui.renderSlot(instance, stack.ingredient().key(), p_282647_, p_281944_);
    }

    @Overwrite
    private BigItemStack lambda$updateConfigs$0(FactoryPanelConnection connection) {
        FactoryPanelBehaviour b = FactoryPanelBehaviour.at(minecraft.level, connection.from);
        if (b == null)
            return new BigItemStack(ItemStack.EMPTY, 0);

        return BigIngredientStack.of(BoardIngredient.of(b), connection.amount).asStack();
    }

    @Redirect(
            method = "updateConfigs",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            )
    )
    private BigItemStack setOutputConfig(ItemStack stack, int count) {
        return BigIngredientStack.of(BoardIngredient.of(behaviour), count).asStack();
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isInputIngredientEmpty(ItemStack instance, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        return stack.ingredient().isEmpty();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isInputIngredientEmptyScroll(ItemStack instance, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        return stack.ingredient().isEmpty();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 0
            )
    )
    private int getInputIngredientCount(BigItemStack instance) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        return stack.getCount();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 2
            )
    )
    private int getOutputIngredientCount(BigItemStack instance) {
        BigIngredientStack stack = (BigIngredientStack) instance;
        return stack.getCount();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 1
            )
    )
    private void setInputIngredientCount(BigItemStack instance, int value) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(value);
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 3
            )
    )
    private void setOutputIngredientCount(BigItemStack instance, int value) {
        BigIngredientStack stack = (BigIngredientStack) instance;

        stack.setCount(value);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(III)I",
                    ordinal = 0
            ),
            remap = true
    )
    private int scrollInputClampRemoval(int value, int min, int max, Operation<Integer> original, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        int maxStackSize = IngredientGui.maxStackSize(stack.ingredient().key());

        if (maxStackSize < 0)
            return Math.max(1, value);

        return original.call(value, min, maxStackSize);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(III)I",
                    ordinal = 1
            ),
            remap = true
    )
    private int scrollOutputClampRemoval(int value, int min, int max, Operation<Integer> original) {
        BigIngredientStack stack = (BigIngredientStack) outputConfig;

        int maxStackSize = IngredientGui.maxStackSize(stack.ingredient().key());

        if (maxStackSize < 0)
            return Math.max(1, value);

        return original.call(value, min, maxStackSize);
    }
}
