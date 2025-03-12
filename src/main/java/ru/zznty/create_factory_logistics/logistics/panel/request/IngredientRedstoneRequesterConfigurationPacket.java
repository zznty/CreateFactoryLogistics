package ru.zznty.create_factory_logistics.logistics.panel.request;

import java.util.List;

public interface IngredientRedstoneRequesterConfigurationPacket {
    List<BigIngredientStack> getStacks();

    void setStacks(List<BigIngredientStack> stacks);
}
