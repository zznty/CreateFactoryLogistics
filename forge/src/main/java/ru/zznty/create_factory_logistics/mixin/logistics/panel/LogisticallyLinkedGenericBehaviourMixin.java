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
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackagerLinkBlockEntity;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_abstractions.generic.support.LogisticallyLinkedGenericBehaviour;

@Mixin(LogisticallyLinkedBehaviour.class)
public abstract class LogisticallyLinkedGenericBehaviourMixin extends BlockEntityBehaviour implements LogisticallyLinkedGenericBehaviour {
    public LogisticallyLinkedGenericBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public Pair<PackagerBlockEntity, GenericRequest> processRequest(GenericStack stack, String address, int linkIndex,
                                                                    MutableBoolean finalLink, int orderId,
                                                                    @Nullable GenericOrder orderContext,
                                                                    @Nullable IdentifiedInventory ignoredHandler) {
        if (blockEntity instanceof GenericPackagerLinkBlockEntity plbe)
            return plbe.processRequest(stack, address, linkIndex, finalLink, orderId, orderContext,
                                       ignoredHandler);

        return null;
    }

}
