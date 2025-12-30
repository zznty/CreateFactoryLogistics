package ru.zznty.create_factory_abstractions.compat.computercraft;

import com.simibubi.create.api.registry.SimpleRegistry;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.resources.ResourceLocation;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.Map;

public interface GenericStackParser {
    SimpleRegistry<GenericKeyRegistration, GenericStackParser> REGISTRY = SimpleRegistry.create();

    GenericStack parse(Map<?, ?> data) throws LuaException;

    static GenericStack parseAny(Map<?, ?> data) throws LuaException {
        GenericStackParser parser = null;
        if (data.get("key") instanceof String key) {
            ResourceLocation keyLoc = ResourceLocation.tryParse(key);
            GenericKeyRegistration registration = GenericContentExtender.REGISTRY.get().getValue(keyLoc);
            if (registration != null)
                parser = REGISTRY.get(registration);

            if (parser == null)
                throw new LuaException("No parser found for key: " + key);
        } else {
            parser = REGISTRY.get(GenericContentExtender.REGISTRATIONS.get(ItemKey.class));
        }

        if (parser == null) {
            throw new LuaException("No parser found for data: " + data);
        }

        return parser.parse(data);
    }
}
