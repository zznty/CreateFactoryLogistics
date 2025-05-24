package ru.zznty.create_factory_abstractions.generic.key;

import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;

@ApiStatus.Internal
public record EmptyKey() implements GenericKey {
}
