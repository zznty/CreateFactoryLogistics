package ru.zznty.create_factory_abstractions.compat.computercraft;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.Map;
import java.util.Optional;

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
            if  (resourceLocation == null) return GenericStack.EMPTY;
            Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.getHolder(resourceLocation);
            if (item.isEmpty()) return GenericStack.EMPTY;

            DataComponentPatch components = DataComponentPatch.EMPTY;
            if (data.get("tag") instanceof Map<?, ?> tagData) {
                CompoundTag tag = LuaNbtUntil.parseTag(tagData);
                components = CatnipCodecUtils.decode(DataComponentPatch.CODEC, tag).orElse(components);
            }

            return GenericStack.wrap(new ItemStack(item.get(), 1, components)).withAmount(count);
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
