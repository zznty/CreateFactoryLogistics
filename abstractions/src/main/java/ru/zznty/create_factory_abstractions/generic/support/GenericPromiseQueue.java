package ru.zznty.create_factory_abstractions.generic.support;

import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

public interface GenericPromiseQueue {
    void add(GenericStack stack);

    void forceClear(GenericStack stack);

    int getTotalPromisedAndRemoveExpired(GenericStack stack, int expiryTime);

    void stackEnteredSystem(GenericStack stack);
}
