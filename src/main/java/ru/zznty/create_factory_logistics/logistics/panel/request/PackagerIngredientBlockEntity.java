package ru.zznty.create_factory_logistics.logistics.panel.request;

import net.minecraft.core.BlockPos;

import java.util.List;

public interface PackagerIngredientBlockEntity {
    void attemptToSendIngredients(List<IngredientRequest> queuedRequests);

    BlockPos getLink();
}
