package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlock;

@Mixin(FactoryPanelConnectionHandler.class)
public class FactoryPanelConnectionHandlerMixin {
    @ModifyExpressionValue(
            method = "checkForIssues(Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;)Ljava/lang/String;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;getBlockState()Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 0
            )
    )
    private static BlockState replaceFluidPanelBlockState1(BlockState original) {
        if (original.getBlock() instanceof FactoryFluidPanelBlock) {
            return createFactoryLogistics$copyBlockState(original);
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "checkForIssues(Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;)Ljava/lang/String;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;getBlockState()Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 1
            ),
            remap = true
    )
    private static BlockState replaceFluidPanelBlockState2(BlockState original) {
        if (original.getBlock() instanceof FactoryFluidPanelBlock) {
            return createFactoryLogistics$copyBlockState(original);
        }

        return original;
    }

    @Unique
    private static @NotNull BlockState createFactoryLogistics$copyBlockState(BlockState original) {
        return AllBlocks.FACTORY_GAUGE.getDefaultState()
                .setValue(FactoryPanelBlock.WATERLOGGED, original.getValue(FactoryPanelBlock.WATERLOGGED))
                .setValue(FactoryPanelBlock.POWERED, original.getValue(FactoryPanelBlock.POWERED))
                .setValue(FactoryPanelBlock.FACING, original.getValue(FactoryPanelBlock.FACING))
                .setValue(FactoryPanelBlock.FACE, original.getValue(FactoryPanelBlock.FACE));
    }

    @ModifyExpressionValue(
            method = "panelClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;",
                    ordinal = 0
            ),
            remap = true
    )
    private static Component fixTooltipFluidNameFrom(Component original, @Local(argsOnly = true) FactoryPanelBehaviour panel) {
        if (panel instanceof FactoryFluidPanelBehaviour fluidPanel) {
            return fluidPanel.getFluid().getDisplayName();
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "panelClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;",
                    ordinal = 1
            ),
            remap = true
    )
    private static Component fixTooltipFluidNameTo(Component original, @Local(ordinal = 1) FactoryPanelBehaviour panel) {
        if (panel instanceof FactoryFluidPanelBehaviour fluidPanel) {
            return fluidPanel.getFluid().getDisplayName();
        }

        return original;
    }
}
