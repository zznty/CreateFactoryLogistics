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
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
    @Shadow(remap = false)
    private FactoryPanelBehaviour behaviour;

    @Shadow(remap = false)
    private BigItemStack outputConfig;

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/box/PackageStyles;getDefaultBox()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
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
            ),
            remap = false
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
            ),
            remap = false
    )
    private GuiGameElement.GuiRenderBuilder filterItem(ItemStack stack,
                                                       Operation<GuiGameElement.GuiRenderBuilder> original) {
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
            ),
            remap = false
    )
    private void renderOutputConfig(GuiGraphics instance, ItemStack p_281978_, int p_282647_, int p_281944_) {
        GenericStack stack = GenericStack.of(behaviour);

        GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .renderSlot(instance, stack.key(), p_282647_, p_281944_);
    }

    /*@Definition(id = "behaviour", field = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelScreen;behaviour:Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;")
    @Definition(id = "getFilter", method = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;getFilter()Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "getHoverName", method = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;")
    @Definition(id = "getString", method = "Lnet/minecraft/network/chat/Component;getString()Ljava/lang/String;")
    @Definition(id = "text", method = "Lcom/simibubi/create/foundation/utility/CreateLang;text(Ljava/lang/String;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Expression("text(this.behaviour.getFilter().getHoverName().getString() + ' x' + ?)")
    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )*/
    @Redirect(
            method = "renderWindow",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=gui.factory_panel.left_click_reset",
                    shift = At.Shift.BY, by = -4
            ),
            remap = false
    )
    private LangBuilder promiseTipValueFormat(String text) {
        GenericStack stack = GenericStack.of(behaviour);

        return GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .nameBuilder(stack.key(), behaviour.getPromised());
    }

    @Definition(
            id = "itemName",
            method = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;"
    )
    @Definition(
            id = "add",
            method = "Lnet/createmod/catnip/lang/LangBuilder;add(Lnet/createmod/catnip/lang/LangBuilder;)Lnet/createmod/catnip/lang/LangBuilder;"
    )
    @Expression("itemName(?).add(?)")
    @Redirect(
            method = "renderWindow",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private LangBuilder outputConfigTipFormat(LangBuilder instance, LangBuilder otherBuilder) {
        GenericStack stack = GenericStack.of(behaviour);

        return GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .nameBuilder(stack.key(), outputConfig.count);
    }

    @Redirect(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    ordinal = 1
            ),
            remap = false
    )
    private void renderPromised(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1) {
        GenericStack stack = GenericStack.of(behaviour);

        GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .renderDecorations(instance, stack.key(), behaviour.getPromised(), j, k);
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
        GenericStack stack = BigGenericStack.of(outputConfig).get();

        GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .renderDecorations(instance, stack.key(), stack.amount(), j, k);
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;",
                    ordinal = 0
            ),
            remap = false
    )
    private LangBuilder inputConfigRestockerTipFormat(ItemStack $, @Local(argsOnly = true) BigItemStack itemStack) {
        GenericStack stack = BigGenericStack.of(itemStack).get();

        return GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .nameBuilder(stack.key());
    }

    @Definition(
            id = "itemName",
            method = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;"
    )
    @Definition(
            id = "add",
            method = "Lnet/createmod/catnip/lang/LangBuilder;add(Lnet/createmod/catnip/lang/LangBuilder;)Lnet/createmod/catnip/lang/LangBuilder;"
    )
    @Expression("itemName(?).add(?)")
    @Redirect(
            method = "renderInputItem",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private LangBuilder inputConfigTipFormat(LangBuilder instance, LangBuilder otherBuilder,
                                             @Local(argsOnly = true) BigItemStack itemStack) {
        GenericStack stack = BigGenericStack.of(itemStack).get();

        return GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .nameBuilder(stack.key(), stack.amount());
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            )
    )
    private void inputConfigAmount(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1,
                                   @Local(argsOnly = true) BigItemStack itemStack) {
        GenericStack stack = BigGenericStack.of(itemStack).get();

        GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .renderDecorations(instance, stack.key(), stack.amount(), j, k);
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"
            )
    )
    private void renderInputFluid(GuiGraphics instance, ItemStack p_281978_, int p_282647_, int p_281944_,
                                  @Local(argsOnly = true) BigItemStack itemStack) {
        GenericStack stack = BigGenericStack.of(itemStack).get();

        GenericContentExtender.registrationOf(stack.key()).clientProvider().guiHandler()
                .renderSlot(instance, stack.key(), p_282647_, p_281944_);
    }

    @Overwrite(remap = false)
    private BigItemStack lambda$updateConfigs$0(FactoryPanelConnection connection) {
        FactoryPanelBehaviour b = FactoryPanelBehaviour.at(minecraft.level, connection.from);
        if (b == null)
            return new BigItemStack(ItemStack.EMPTY, 0);

        return BigGenericStack.of(GenericStack.of(b).withAmount(connection.amount)).asStack();
    }

    @Redirect(
            method = "updateConfigs",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            ),
            remap = false
    )
    private BigItemStack setOutputConfig(ItemStack stack, int count) {
        return BigGenericStack.of(GenericStack.of(behaviour).withAmount(count)).asStack();
    }

    @Redirect(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isInputIngredientEmpty(ItemStack instance, @Local(argsOnly = true) BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);

        return stack.get().isEmpty();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean isInputIngredientEmptyScroll(ItemStack instance, @Local BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);

        return stack.get().isEmpty();
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 1,
                    remap = false
            )
    )
    private void setInputIngredientCount(BigItemStack instance, int value) {
        BigGenericStack stack = BigGenericStack.of(instance);

        stack.setAmount(value);
    }

    @Redirect(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 3,
                    remap = false
            )
    )
    private void setOutputIngredientCount(BigItemStack instance, int value) {
        BigGenericStack stack = BigGenericStack.of(instance);

        stack.setAmount(value);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(III)I",
                    ordinal = 0
            )
    )
    private int scrollInputClampRemoval(int value, int min, int max, Operation<Integer> original,
                                        @Local BigItemStack itemStack) {
        BigGenericStack stack = BigGenericStack.of(itemStack);

        int maxStackSize = GenericContentExtender.registrationOf(stack.get().key()).clientProvider().guiHandler()
                .maxStackSize(stack.get().key());

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
            )
    )
    private int scrollOutputClampRemoval(int value, int min, int max, Operation<Integer> original) {
        BigGenericStack stack = BigGenericStack.of(outputConfig);

        int maxStackSize = GenericContentExtender.registrationOf(stack.get().key()).clientProvider().guiHandler()
                .maxStackSize(stack.get().key());

        if (maxStackSize < 0)
            return Math.max(1, value);

        return original.call(value, min, maxStackSize);
    }
}
