package ru.zznty.create_factory_abstractions.generic.support;

import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public interface GenericRedstoneRequesterConfigurationPacket {
    List<GenericStack> getStacks();

    void setStacks(List<GenericStack> stacks);
}
