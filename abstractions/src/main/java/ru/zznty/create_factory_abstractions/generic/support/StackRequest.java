package ru.zznty.create_factory_abstractions.generic.support;

import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.UUID;

public record StackRequest(GenericStack stack, UUID network) {
}
