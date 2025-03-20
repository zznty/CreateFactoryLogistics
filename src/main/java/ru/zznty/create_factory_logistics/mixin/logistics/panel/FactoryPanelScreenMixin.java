package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.utility.CreateLang;
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
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;

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
    private GuiGameElement.GuiRenderBuilder filterItem(ItemStack stack, Operation<GuiGameElement.GuiRenderBuilder> original) {
        return behaviour instanceof FactoryFluidPanelBehaviour ?
                GuiGameElement.of(Blocks.AIR) :
                original.call(stack);
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V",
                    ordinal = 0
            ),
            remap = false
    )
    private void renderOutputConfig(GuiGraphics instance, ItemStack p_281978_, int p_282647_, int p_281944_, Operation<Void> original) {
        if (behaviour instanceof FactoryFluidPanelBehaviour fluidBehaviour) {
            GuiGameElement.of(fluidBehaviour.getFluid().getFluid())
                    .scale(15)
                    .atLocal(1 / 32f, 1 + 1 / 32f, 2)
                    .render(instance, p_282647_, p_281944_);

            return;
        }

        original.call(instance, p_281978_, p_282647_, p_281944_);
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
    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=gui.factory_panel.left_click_reset",
                    shift = At.Shift.BY, by = -4
            ),
            remap = false
    )
    private LangBuilder promiseTipValueFormat(String text, Operation<LangBuilder> original) {
        if (behaviour instanceof FactoryFluidPanelBehaviour fluidBehaviour) {
            return CreateLang.builder()
                    .add(fluidBehaviour.getFluid().getDisplayName())
                    .space()
                    .add(FactoryFluidPanelBehaviour.formatLevel(fluidBehaviour.getPromised()));
        }

        return original.call(text);
    }

    @Definition(id = "itemName", method = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Definition(id = "add", method = "Lnet/createmod/catnip/lang/LangBuilder;add(Lnet/createmod/catnip/lang/LangBuilder;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Expression("itemName(?).add(?)")
    @ModifyExpressionValue(
            method = "renderWindow",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private LangBuilder outputConfigTipFormat(LangBuilder original) {
        if (behaviour instanceof FactoryFluidPanelBehaviour fluidBehaviour) {
            return CreateLang.builder()
                    .add(fluidBehaviour.getFluid().getDisplayName())
                    .space()
                    .add(FactoryFluidPanelBehaviour.formatLevel(outputConfig.count));
        }

        return original;
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    ordinal = 1
            ),
            remap = false
    )
    private void renderPromised(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1, Operation<Void> original) {
        if (behaviour instanceof FactoryFluidPanelBehaviour fluidBehaviour) {
            int promised = fluidBehaviour.getPromised();
            if (promised > 0) {
                i1 = FactoryFluidPanelBehaviour.formatLevel(promised).string();
            }
        }

        original.call(instance, l, i, j, k, i1);
    }

    @WrapOperation(
            method = "renderWindow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                    ordinal = 0
            ),
            remap = true
    )
    private void outputConfigAmount(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1, Operation<Void> original) {
        if (behaviour instanceof FactoryFluidPanelBehaviour) {
            i1 = FactoryFluidPanelBehaviour.formatLevel(outputConfig.count).string();
        }

        original.call(instance, l, i, j, k, i1);
    }

    @ModifyExpressionValue(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;",
                    ordinal = 0
            ),
            remap = false
    )
    private LangBuilder inputConfigRestockerTipFormat(LangBuilder original, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            return CreateLang.builder().add(fluidIngredient.stack().getDisplayName());
        }
        return original;
    }

    @Definition(id = "itemName", method = "Lcom/simibubi/create/foundation/utility/CreateLang;itemName(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Definition(id = "add", method = "Lnet/createmod/catnip/lang/LangBuilder;add(Lnet/createmod/catnip/lang/LangBuilder;)Lnet/createmod/catnip/lang/LangBuilder;")
    @Expression("itemName(?).add(?)")
    @ModifyExpressionValue(
            method = "renderInputItem",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            remap = false
    )
    private LangBuilder inputConfigTipFormat(LangBuilder original, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            return CreateLang.builder()
                    .add(fluidIngredient.stack().getDisplayName())
                    .space()
                    .add(FactoryFluidPanelBehaviour.formatLevel(fluidIngredient.amount()));
        }
        return original;
    }

    @WrapOperation(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
            ),
            remap = true
    )
    private void inputConfigAmount(GuiGraphics instance, Font l, ItemStack i, int j, int k, String i1, Operation<Void> original, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;
        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            i1 = FactoryFluidPanelBehaviour.formatLevel(fluidIngredient.amount()).string();
            i = fluidIngredient.stack().getFluid().getBucket().getDefaultInstance();
        }

        original.call(instance, l, i, j, k, i1);
    }

    @WrapOperation(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;II)V"
            ),
            remap = true
    )
    private void renderInputFluid(GuiGraphics instance, ItemStack p_281978_, int p_282647_, int p_281944_, Operation<Void> original, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            GuiGameElement.of(fluidIngredient.stack().getFluid())
                    .scale(15)
                    .atLocal(1 / 32f, 1 + 1 / 32f, 2)
                    .render(instance, p_282647_, p_281944_);
        } else {
            original.call(instance, p_281978_, p_282647_, p_281944_);
        }
    }

    @Overwrite(remap = false)
    private BigItemStack lambda$updateConfigs$0(FactoryPanelConnection connection) {
        FactoryPanelBehaviour b = FactoryPanelBehaviour.at(minecraft.level, connection.from);
        if (b == null)
            return new BigItemStack(ItemStack.EMPTY, 0);

        return BigIngredientStack.of(BoardIngredient.of(b), connection.amount).asStack();
    }

    @WrapOperation(
            method = "updateConfigs",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/item/ItemStack;I)Lcom/simibubi/create/content/logistics/BigItemStack;"
            ),
            remap = false
    )
    private BigItemStack setOutputConfig(ItemStack stack, int count, Operation<BigItemStack> original) {
        return BigIngredientStack.of(BoardIngredient.of(behaviour), count).asStack();
    }

    @ModifyExpressionValue(
            method = "renderInputItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            ),
            remap = true
    )
    private boolean isInputIngredientEmpty(boolean original, @Local(argsOnly = true) BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        return stack.getIngredient() == BoardIngredient.EMPTY;
    }

    @ModifyExpressionValue(
            method = "mouseScrolled",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            ),
            remap = true
    )
    private boolean isInputIngredientEmptyScroll(boolean original, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        return stack.getIngredient() == BoardIngredient.EMPTY;
    }

    @ModifyExpressionValue(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 0,
                    remap = false
            )
    )
    private int getInputIngredientCount(int original, @Local BigItemStack itemStack) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        return stack.getCount();
    }

    @ModifyExpressionValue(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 2,
                    remap = false
            )
    )
    private int getOutputIngredientCount(int original) {
        BigIngredientStack stack = (BigIngredientStack) outputConfig;

        return stack.getCount();
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 1,
                    remap = false
            )
    )
    private void setInputIngredientCount(BigItemStack itemStack, int value, Operation<Void> original) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

        stack.setCount(value);
    }

    @WrapOperation(
            method = "mouseScrolled",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/logistics/BigItemStack;count:I",
                    ordinal = 3,
                    remap = false
            )
    )
    private void setOutputIngredientCount(BigItemStack itemStack, int value, Operation<Void> original) {
        BigIngredientStack stack = (BigIngredientStack) itemStack;

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

        if (stack.getIngredient() instanceof FluidBoardIngredient)
            return Math.max(1, value);

        return original.call(value, min, max);
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

        if (stack.getIngredient() instanceof FluidBoardIngredient)
            return Math.max(1, value);

        return original.call(value, min, max);
    }
}
