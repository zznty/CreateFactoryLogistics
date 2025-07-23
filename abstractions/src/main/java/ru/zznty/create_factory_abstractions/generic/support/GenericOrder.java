package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;

import java.util.ArrayList;
import java.util.List;

public record GenericOrder(List<GenericStack> stacks, List<PackageOrderWithCrafts.CraftingEntry> crafts) {

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("Entries", NBTHelper.writeCompoundList(stacks, s -> {
            CompoundTag stackTag = new CompoundTag();
            GenericStackSerializer.write(s, stackTag);
            return stackTag;
        }));
        tag.put("Crafts", NBTHelper.writeCompoundList(crafts, PackageOrderWithCrafts.CraftingEntry::write));
        return tag;
    }

    public static GenericOrder empty() {
        return new GenericOrder(List.of(), List.of());
    }

    public static GenericOrder order(List<GenericStack> stacks) {
        return new GenericOrder(stacks, List.of());
    }

    public static GenericOrder craftingOrder(List<GenericStack> stacks, List<BigItemStack> craftPattern) {
        return new GenericOrder(stacks,
                                List.of(new PackageOrderWithCrafts.CraftingEntry(new PackageOrder(craftPattern),
                                                                                 1)));
    }

    public static GenericOrder of(PanelRequestedStacks context, List<StackRequest> ingredients) {
        List<GenericStack> list = ingredients.stream().map(StackRequest::stack).toList();
        return context.hasCraftingContext() ?
               craftingOrder(list, context.craftingContext()) :
               order(list);
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public static GenericOrder read(CompoundTag tag) {
        ListTag listTag = tag.getList("Entries", Tag.TAG_COMPOUND);
        List<GenericStack> stacks = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                                      entryTag -> stacks.add(GenericStackSerializer.read(entryTag)));
        listTag = tag.getList("Crafts", Tag.TAG_COMPOUND);
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                                      entryTag -> crafts.add(PackageOrderWithCrafts.CraftingEntry.read(entryTag)));
        return new GenericOrder(stacks, crafts);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(stacks.size());
        for (GenericStack stack : stacks)
            GenericStackSerializer.write(stack, buffer);
        buffer.writeVarInt(crafts.size());
        for (PackageOrderWithCrafts.CraftingEntry entry : crafts)
            entry.write(buffer);
    }

    public PackageOrderWithCrafts asCrafting() {
        return new PackageOrderWithCrafts(
                new PackageOrder(stacks.stream().map(BigGenericStack::of).map(BigGenericStack::asStack).toList()),
                crafts);
    }

    public static GenericOrder of(PackageOrderWithCrafts orderWithCrafts) {
        return new GenericOrder(
                orderWithCrafts.stacks().stream().map(BigGenericStack::of).map(BigGenericStack::get).toList(),
                orderWithCrafts.orderedCrafts());
    }

    public static GenericOrder read(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<GenericStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            stacks.add(GenericStackSerializer.read(buffer));
        int craftSize = buffer.readVarInt();
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(craftSize);
        for (int i = 0; i < craftSize; i++)
            crafts.add(PackageOrderWithCrafts.CraftingEntry.read(buffer));
        return new GenericOrder(stacks, crafts);
    }

    public static void set(ItemStack box, int orderId, int linkIndex, boolean isFinalLink, int fragmentIndex,
                           boolean isFinal, @Nullable GenericOrder orderContext) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("OrderId", orderId);
        tag.putInt("LinkIndex", linkIndex);
        tag.putBoolean("IsFinalLink", isFinalLink);
        tag.putInt("Index", fragmentIndex);
        tag.putBoolean("IsFinal", isFinal);
        if (orderContext != null) {
            PackageOrderWithCrafts craftingOrder = orderContext.asCrafting();
            tag.put("OrderContext", craftingOrder.write());
        }
        box.getOrCreateTag()
                .put("Fragment", tag);
    }
}
