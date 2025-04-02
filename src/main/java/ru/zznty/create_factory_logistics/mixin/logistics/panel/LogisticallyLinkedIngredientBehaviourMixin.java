package ru.zznty.create_factory_logistics.mixin.logistics.panel;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRequest;
import ru.zznty.create_factory_logistics.logistics.panel.request.LogisticallyLinkedIngredientBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.request.PackagerIngredientLinkBlockEntity;

@Mixin(LogisticallyLinkedBehaviour.class)
public abstract class LogisticallyLinkedIngredientBehaviourMixin extends BlockEntityBehaviour implements LogisticallyLinkedIngredientBehaviour {
    public LogisticallyLinkedIngredientBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public Pair<PackagerBlockEntity, IngredientRequest> processRequest(BoardIngredient ingredient, String address, int linkIndex, MutableBoolean finalLink, int orderId, @Nullable IngredientOrder orderContext, @javax.annotation.Nullable IdentifiedInventory ignoredHandler) {
        if (blockEntity instanceof PackagerIngredientLinkBlockEntity plbe)
            return plbe.processRequest(ingredient, address, linkIndex, finalLink, orderId, orderContext,
                    ignoredHandler);

        return null;
    }

}
