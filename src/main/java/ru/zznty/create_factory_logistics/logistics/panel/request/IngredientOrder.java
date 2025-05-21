package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record IngredientOrder(List<BigIngredientStack> stacks, List<PackageOrderWithCrafts.CraftingEntry> crafts) {
    public CompoundTag write(HolderLookup.Provider levelRegistryAccess) {
        CompoundTag tag = new CompoundTag();
        tag.put("Entries", NBTHelper.writeCompoundList(stacks, s -> {
            CompoundTag compoundTag = new CompoundTag();
            s.ingredient().write(levelRegistryAccess, compoundTag);
            return compoundTag;
        }));
        tag.put("Crafts", NBTHelper.writeCompoundList(crafts, e -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("Entries", NBTHelper.writeCompoundList(e.pattern().stacks(), s -> {
                CompoundTag tag1 = new CompoundTag();
                ((BigIngredientStack) s).ingredient().write(levelRegistryAccess, tag1);
                return tag1;
            }));
            compoundTag.putInt("Count", e.count());
            return compoundTag;
        }));
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

    public PackageOrderWithCrafts asCrafting() {
        return new PackageOrderWithCrafts(new PackageOrder(stacks.stream().map(BigIngredientStack::asStack).toList()), crafts);
    }

    public static IngredientOrder read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        ListTag listTag = tag.getList("Entries", Tag.TAG_COMPOUND);
        List<BigIngredientStack> stacks = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                entryTag -> stacks.add(BigIngredientStack.of(BoardIngredient.read(levelRegistryAccess, entryTag))));
        listTag = tag.getList("Crafts", Tag.TAG_COMPOUND);
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                entryTag -> {
                    PackageOrder order = new PackageOrder(NBTHelper.readCompoundList(entryTag.getList("Entries", Tag.TAG_COMPOUND), t -> BigIngredientStack.of(BoardIngredient.read(levelRegistryAccess, t)).asStack()));
                    crafts.add(new PackageOrderWithCrafts.CraftingEntry(order, entryTag.getInt("Count")));
                });
        return new IngredientOrder(stacks, crafts);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(stacks.size());
        for (BigIngredientStack entry : stacks)
            entry.ingredient().write(buffer);
        buffer.writeVarInt(crafts.size());
        for (PackageOrderWithCrafts.CraftingEntry entry : crafts) {
            PackageOrder pattern = entry.pattern();
            buffer.writeVarInt(pattern.stacks().size());
            for (BigItemStack stack : pattern.stacks()) {
                BigIngredientStack ingredient = (BigIngredientStack) stack;
                ingredient.ingredient().write(buffer);
            }
            buffer.writeVarInt(entry.count());
        }
    }

    public static IngredientOrder read(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<BigIngredientStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            stacks.add((BigIngredientStack) BigItemStack.receive(buffer));
        int craftSize = buffer.readVarInt();
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(craftSize);
        for (int i = 0; i < craftSize; i++) {
            int patternSize = buffer.readVarInt();
            List<BigItemStack> pattern = new ArrayList<>(patternSize);
            for (int j = 0; j < patternSize; j++)
                pattern.add(BigIngredientStack.of(BoardIngredient.read(buffer)).asStack());
            crafts.add(new PackageOrderWithCrafts.CraftingEntry(new PackageOrder(pattern), buffer.readVarInt()));
        }
        return new IngredientOrder(stacks, crafts);
    }

    public static void set(HolderLookup.Provider levelRegistryAccess, ItemStack box, int orderId, int linkIndex, boolean isFinalLink, int fragmentIndex,
                           boolean isFinal, @Nullable IngredientOrder orderContext) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("OrderId", orderId);
        tag.putInt("LinkIndex", linkIndex);
        tag.putBoolean("IsFinalLink", isFinalLink);
        tag.putInt("Index", fragmentIndex);
        tag.putBoolean("IsFinal", isFinal);
        if (orderContext != null) {
            tag.put("OrderContext", orderContext.write(levelRegistryAccess));
        }
        CustomData.update(DataComponents.CUSTOM_DATA, box, t -> t.put("Fragment", tag));
    }

    public static IngredientOrder of(PackageOrderWithCrafts orderWithCrafts) {
        return new IngredientOrder(orderWithCrafts.stacks().stream().map(it -> (BigIngredientStack) it).toList(), orderWithCrafts.orderedCrafts());
    }

    public static IngredientOrder of(HolderLookup.Provider levelRegistryAccess, ItemStack box) {
        CustomData data = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.contains("Fragment"))
            return null;
        CompoundTag frag = data.copyTag().getCompound("Fragment");
        if (!frag.contains("OrderContext"))
            return null;
        return IngredientOrder.read(levelRegistryAccess, frag.getCompound("OrderContext"));
    }
}
