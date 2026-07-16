package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;

import java.util.Collection;

/**
 * A destination for logistics package requests. Implemented both by the vanilla
 * {@link PackagerBlockEntity} (via {@link #ofPackager}) and by external handlers
 * such as the AE2 interface integration, so {@link GenericLogisticsManager} can
 * route and dispatch requests polymorphically without special-casing.
 */
public interface GenericPackageTarget {
    /**
     * @return whether this target has accumulated too many pending packages to accept the given request type
     */
    boolean isTooBusyFor(LogisticallyLinkedBehaviour.RequestType type);

    /**
     * Builds and dispatches packages for the given queued requests. Implementations should
     * fulfill as much of each request as possible, potentially producing multiple packages.
     *
     * @param queuedRequests requests routed to this target; may be mutated/cleared as fulfilled
     */
    void dispatch(Collection<GenericRequest> queuedRequests);

    /**
     * Wraps a vanilla packager as a dispatch target. equals/hashCode delegate to the
     * underlying block entity so requests to the same packager group together.
     */
    static GenericPackageTarget ofPackager(PackagerBlockEntity packager) {
        return new PackagerTarget(packager);
    }

    record PackagerTarget(PackagerBlockEntity packager) implements GenericPackageTarget {
        @Override
        public boolean isTooBusyFor(LogisticallyLinkedBehaviour.RequestType type) {
            return packager.isTooBusyFor(type);
        }

        @Override
        public void dispatch(Collection<GenericRequest> queuedRequests) {
            if (queuedRequests.isEmpty())
                return;

            packager.flashLink();

            GenericPackagerBlockEntity genericPackager = GenericPackagerBlockEntity.from(packager);
            for (int i = 0; i < 100 && !queuedRequests.isEmpty(); i++)
                genericPackager.attemptToSendGeneric(queuedRequests);

            packager.triggerStockCheck();
            packager.notifyUpdate();
        }
    }
}
