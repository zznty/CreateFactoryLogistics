package ru.zznty.create_factory_abstractions.compat.computercraft;

import com.simibubi.create.api.registry.SimpleRegistry;
import dan200.computercraft.api.lua.LuaException;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface GenericDetailsProvider<Key extends GenericKey> {
    SimpleRegistry<GenericKeyRegistration, GenericDetailsProvider<?>> REGISTRY = SimpleRegistry.create();

    Map<String, ?> detail(Key key) throws LuaException;

    static Map<Integer, Map<String, ?>> details(GenericInventorySummary summary) throws LuaException {
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        List<GenericStack> stacks = summary.get();
        for (int i = 0; i < stacks.size(); i++) {
            GenericStack stack = stacks.get(i);
            result.put(i, detail(stack));
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static Map<String, ?> detail(GenericStack stack) throws LuaException {
        if (stack.isEmpty()) return null;
        GenericDetailsProvider provider = REGISTRY.get(
                GenericContentExtender.registrationOf(stack.key()));
        if (provider == null)
            return null;

        Map<String, Object> details = new HashMap<>(provider.detail(stack.key()));
        details.put("count", stack.amount());
        details.put("amount", stack.amount());

        return details;
    }
}
