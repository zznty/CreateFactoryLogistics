package ru.zznty.create_factory_abstractions.generic.support;

import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public interface GenericGhostMenu {
    GenericStack getGenericSlot(int slot);

    void setSlot(int slot, GenericStack stack);

    List<GenericStack> getStacks();
}
