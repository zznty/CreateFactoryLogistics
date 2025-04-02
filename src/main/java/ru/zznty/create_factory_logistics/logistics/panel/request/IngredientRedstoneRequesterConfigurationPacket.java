package ru.zznty.create_factory_logistics.logistics.panel.request;

import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;

import java.util.List;

public interface IngredientRedstoneRequesterConfigurationPacket {
    List<BigIngredientStack> getStacks();

    void setStacks(List<BigIngredientStack> stacks);
}
