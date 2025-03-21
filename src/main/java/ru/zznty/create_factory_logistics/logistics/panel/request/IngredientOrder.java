package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record IngredientOrder(List<BigIngredientStack> stacks) {
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("Entries", NBTHelper.writeCompoundList(stacks, s -> s.asStack().write()));
        return tag;
    }

    public static IngredientOrder empty() {
        return new IngredientOrder(List.of());
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public static IngredientOrder read(CompoundTag tag) {
        ListTag listTag = tag.getList("Entries", Tag.TAG_COMPOUND);
        List<BigIngredientStack> stacks = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                entryTag -> stacks.add((BigIngredientStack) BigItemStack.read(entryTag)));
        return new IngredientOrder(stacks);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(stacks.size());
        for (BigIngredientStack entry : stacks)
            entry.asStack().send(buffer);
    }

    public static IngredientOrder read(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<BigIngredientStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            stacks.add((BigIngredientStack) BigItemStack.receive(buffer));
        return new IngredientOrder(stacks);
    }

    public static void set(ItemStack box, int orderId, int linkIndex, boolean isFinalLink, int fragmentIndex,
                           boolean isFinal, @Nullable IngredientOrder orderContext) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("OrderId", orderId);
        tag.putInt("LinkIndex", linkIndex);
        tag.putBoolean("IsFinalLink", isFinalLink);
        tag.putInt("Index", fragmentIndex);
        tag.putBoolean("IsFinal", isFinal);
        if (orderContext != null)
            tag.put("OrderContext", orderContext.write());
        box.getOrCreateTag()
                .put("Fragment", tag);
    }

    public static IngredientOrder of(PackageOrderWithCrafts orderWithCrafts) {
        // todo carry crafting information too
        return new IngredientOrder(orderWithCrafts.stacks().stream().map(it -> (BigIngredientStack) it).toList());
    }
}
