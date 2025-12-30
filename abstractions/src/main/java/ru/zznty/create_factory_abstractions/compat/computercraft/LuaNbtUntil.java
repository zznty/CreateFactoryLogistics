package ru.zznty.create_factory_abstractions.compat.computercraft;

import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.Map;

public final class LuaNbtUntil {
    public static CompoundTag parseTag(Map<?, ?> data) {
        CompoundTag tag = new CompoundTag();
        data.forEach((key, value) -> {
            if (key instanceof String stringKey) {
                tag.put(stringKey, parseTagValue(value));
            }
        });

        return tag.isEmpty() ? null : tag;
    }

    public static Tag parseTagValue(Object value) {
        if (value instanceof Map<?, ?> data) return parseTag(data);

        if (value instanceof String str) return StringTag.valueOf(str);
        if (value instanceof Short short_) return ShortTag.valueOf(short_);
        if (value instanceof Integer num) return IntTag.valueOf(num);
        if (value instanceof Long num) return LongTag.valueOf(num);
        if (value instanceof Float num) return FloatTag.valueOf(num);
        if (value instanceof Double num) return DoubleTag.valueOf(num);
        if (value instanceof Boolean bool) return ByteTag.valueOf(bool);
        return null;
    }

    public static Map<String, Object> serializeTag(CompoundTag tag) {
        Map<String, Object> data = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            data.put(key, serializeTag(tag.get(key)));
        }
        return data;
    }

    public static Object serializeTag(Tag tag) {
        if (tag instanceof StringTag stringTag) return stringTag.getAsString();
        if (tag instanceof ShortTag shortTag) return shortTag.getAsShort();
        if (tag instanceof IntTag intTag) return intTag.getAsInt();
        if (tag instanceof LongTag longTag) return longTag.getAsLong();
        if (tag instanceof FloatTag floatTag) return floatTag.getAsFloat();
        if (tag instanceof DoubleTag doubleTag) return doubleTag.getAsDouble();
        if (tag instanceof ByteTag byteTag) return byteTag.getAsByte();
        if (tag instanceof CompoundTag compoundTag) return serializeTag(compoundTag);
        if (tag instanceof ListTag listTag) return serializeTag(listTag);
        return null;
    }

    public static Map<Integer, Object> serializeTag(ListTag tag) {
        Map<Integer, Object> data = new HashMap<>();
        for (int i = 0; i < tag.size(); i++) {
            data.put(i, serializeTag(tag.get(i)));
        }
        return data;
    }
}
