package ru.zznty.create_factory_abstractions.compat.computercraft;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.Map;

public class AbstractionsComputerCraftCompat {
    public static final String MOD_ID = "computercraft";

    public static void register() {
        GenericKeyRegistration itemReg = GenericContentExtender.REGISTRATIONS.get(ItemKey.class);
        GenericDetailsProvider.REGISTRY.register(itemReg,
                                                 (GenericDetailsProvider<ItemKey>) key ->
                                                         VanillaDetailRegistries.ITEM_STACK.getDetails(key.stack()));
        GenericStackParser.REGISTRY.register(itemReg, data -> {
            String itemName = "minecraft:air";
            if (data.get("name") instanceof String) {
                itemName = (String) data.get("name");
            }
            int count = parseCount("count", data);
            if (count < 0)
                count = parseCount("amount", data);
            if (count < 0)
                count = 1;
            if (count > 256)
                throw new LuaException("Count for item " + itemName + " exceeds 256");
            ResourceLocation resourceLocation = ResourceLocation.tryParse(itemName);
            ItemLike item = ForgeRegistries.ITEMS.getValue(resourceLocation);
            if (item == null) return GenericStack.EMPTY;

            CompoundTag tag = null;
            if (data.get("tag") instanceof Map<?, ?> tagData)
                tag = LuaNbtUntil.parseTag(tagData);

            return GenericStack.wrap(new ItemStack(item, 1, tag)).withAmount(count);
        });
    }

    public static int parseCount(String name, Map<?, ?> data) throws LuaException {
        int count = -1;
        if (data.get(name) instanceof Number) {
            Object countObj = data.get(name);
            count = (countObj instanceof Number) ? ((Number) countObj).intValue() : 1;
        }
        return count;
    }
}
