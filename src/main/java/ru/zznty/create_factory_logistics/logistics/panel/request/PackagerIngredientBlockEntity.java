package ru.zznty.create_factory_logistics.logistics.panel.request;

import net.minecraft.core.BlockPos;

import java.util.Collection;

public interface PackagerIngredientBlockEntity {

    /**
     * Tries to send as many requests as possible as one box
     *
     * @param queuedRequests Collection of requests to send, will be cleared as requests are fulfilled
     */
    void attemptToSendIngredients(Collection<IngredientRequest> queuedRequests);

    BlockPos getLink();
}
