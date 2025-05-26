package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.ArrayList;
import java.util.Collection;

public interface GenericPackagerBlockEntity {

    /**
     * Tries to send as many requests as possible as one box
     *
     * @param queuedRequests Collection of requests to send, will be cleared as requests are fulfilled
     */
    void attemptToSendGeneric(Collection<GenericRequest> queuedRequests);

    static GenericPackagerBlockEntity from(PackagerBlockEntity packagerBlockEntity) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return (GenericPackagerBlockEntity) packagerBlockEntity;

        return new GenericPackagerBlockEntity() {
            private final PackagerBlockEntity blockEntity = packagerBlockEntity;

            @Override
            public void attemptToSendGeneric(Collection<GenericRequest> queuedRequests) {
                ArrayList<PackagingRequest> requests = new ArrayList<>(queuedRequests.size());
                for (GenericRequest request : queuedRequests) {
                    if (request.stack().key() instanceof ItemKey key)
                        requests.add(new PackagingRequest(
                                key.stack(),
                                request.count(),
                                request.address(),
                                request.linkIndex(),
                                request.finalLink(),
                                request.packageCounter(),
                                request.orderId(),
                                request.context() == null ? null : request.context().asCrafting()
                        ));
                }
                blockEntity.attemptToSend(requests);
            }
        };
    }
}
