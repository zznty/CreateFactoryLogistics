package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

public interface LogisticallyLinkedGenericBehaviour {
    Pair<PackagerBlockEntity, GenericRequest> processRequest(GenericStack stack, String address,
                                                             int linkIndex, MutableBoolean finalLink, int orderId,
                                                             @Nullable GenericOrder orderContext,
                                                             @Nullable IdentifiedInventory ignoredHandler);

    static LogisticallyLinkedGenericBehaviour from(LogisticallyLinkedBehaviour link) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return (LogisticallyLinkedGenericBehaviour) link;

        return new LogisticallyLinkedGenericBehaviour() {
            private final LogisticallyLinkedBehaviour behaviour = link;

            @Override
            public Pair<PackagerBlockEntity, GenericRequest> processRequest(GenericStack stack, String address,
                                                                            int linkIndex, MutableBoolean finalLink,
                                                                            int orderId,
                                                                            @Nullable GenericOrder orderContext,
                                                                            @Nullable IdentifiedInventory ignoredHandler) {
                if (stack.key() instanceof ItemKey key) {
                    Pair<PackagerBlockEntity, PackagingRequest> pair = behaviour.processRequest(
                            key.stack(), stack.amount(), address, linkIndex, finalLink, orderId,
                            orderContext == null ? null : orderContext.asCrafting(), ignoredHandler);

                    PackagingRequest request = pair.getSecond();
                    return Pair.of(pair.getFirst(), new GenericRequest(
                            GenericStack.wrap(request.item()),
                            request.count(),
                            request.address(),
                            request.linkIndex(),
                            request.finalLink(),
                            request.packageCounter(),
                            request.orderId(),
                            request.context() == null ? null : GenericOrder.of(request.context())
                    ));
                }
                return null;
            }
        };
    }
}
