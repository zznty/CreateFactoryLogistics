package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
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

public record IngredientOrder(List<BigIngredientStack> stacks, List<PackageOrderWithCrafts.CraftingEntry> crafts) {
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("Entries", NBTHelper.writeCompoundList(stacks, s -> s.asStack().write()));
        tag.put("Crafts", NBTHelper.writeCompoundList(crafts, PackageOrderWithCrafts.CraftingEntry::write));
        return tag;
    }

    public static IngredientOrder empty() {
        return new IngredientOrder(List.of(), List.of());
    }

    public static IngredientOrder order(List<BigIngredientStack> stacks) {
        return new IngredientOrder(stacks, List.of());
    }

    public static IngredientOrder craftingOrder(List<BigIngredientStack> stacks, List<BigItemStack> craftPattern) {
        return new IngredientOrder(stacks, List.of(new PackageOrderWithCrafts.CraftingEntry(new PackageOrder(craftPattern), 1)));
    }

    public static IngredientOrder of(PanelRequestedIngredients ingredients) {
        return ingredients.hasCraftingContext() ?
                craftingOrder(ingredients.ingredients(), ingredients.craftingContext()) :
                order(ingredients.ingredients());
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public static IngredientOrder read(CompoundTag tag) {
        ListTag listTag = tag.getList("Entries", Tag.TAG_COMPOUND);
        List<BigIngredientStack> stacks = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                entryTag -> stacks.add((BigIngredientStack) BigItemStack.read(entryTag)));
        listTag = tag.getList("Crafts", Tag.TAG_COMPOUND);
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                entryTag -> crafts.add(PackageOrderWithCrafts.CraftingEntry.read(entryTag)));
        return new IngredientOrder(stacks, crafts);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(stacks.size());
        for (BigIngredientStack entry : stacks)
            entry.asStack().send(buffer);
        buffer.writeVarInt(crafts.size());
        for (PackageOrderWithCrafts.CraftingEntry entry : crafts)
            entry.write(buffer);
    }

    public PackageOrderWithCrafts asCrafting() {
        for (BigIngredientStack stack : stacks) {
            if (!(stack.getIngredient() instanceof ItemBoardIngredient))
                return null;
        }

        return new PackageOrderWithCrafts(new PackageOrder(stacks.stream().map(BigIngredientStack::asStack).toList()), crafts);
    }

    public static IngredientOrder read(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<BigIngredientStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            stacks.add((BigIngredientStack) BigItemStack.receive(buffer));
        int craftSize = buffer.readVarInt();
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(craftSize);
        for (int i = 0; i < craftSize; i++)
            crafts.add(PackageOrderWithCrafts.CraftingEntry.read(buffer));
        return new IngredientOrder(stacks, crafts);
    }

    public static void set(ItemStack box, int orderId, int linkIndex, boolean isFinalLink, int fragmentIndex,
                           boolean isFinal, @Nullable IngredientOrder orderContext) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("OrderId", orderId);
        tag.putInt("LinkIndex", linkIndex);
        tag.putBoolean("IsFinalLink", isFinalLink);
        tag.putInt("Index", fragmentIndex);
        tag.putBoolean("IsFinal", isFinal);
        if (orderContext != null) {
            PackageOrderWithCrafts craftingOrder = orderContext.asCrafting();
            if (craftingOrder != null)
                tag.put("OrderContext", craftingOrder.write());
        }
        box.getOrCreateTag()
                .put("Fragment", tag);
    }

    public static IngredientOrder of(PackageOrderWithCrafts orderWithCrafts) {
        return new IngredientOrder(orderWithCrafts.stacks().stream().map(it -> (BigIngredientStack) it).toList(), orderWithCrafts.orderedCrafts());
    }
}
