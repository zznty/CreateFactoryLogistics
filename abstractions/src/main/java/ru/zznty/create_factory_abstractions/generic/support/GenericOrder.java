package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
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
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;

import java.util.ArrayList;
import java.util.List;

public record GenericOrder(List<GenericStack> stacks, List<PackageOrderWithCrafts.CraftingEntry> crafts) {

    public CompoundTag write(HolderLookup.Provider levelRegistryAccess) {
        CompoundTag tag = new CompoundTag();
        tag.put("Entries", NBTHelper.writeCompoundList(stacks, s -> {
            CompoundTag compoundTag = new CompoundTag();
            GenericStackSerializer.write(levelRegistryAccess, s, compoundTag);
            return compoundTag;
        }));
        tag.put("Crafts", NBTHelper.writeCompoundList(crafts, e -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("Entries", NBTHelper.writeCompoundList(e.pattern().stacks(), s -> {
                CompoundTag tag1 = new CompoundTag();
                GenericStackSerializer.write(levelRegistryAccess, BigGenericStack.of(s).get(), tag1);
                return tag1;
            }));
            compoundTag.putInt("Count", e.count());
            return compoundTag;
        }));
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

    public static GenericOrder read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        ListTag listTag = tag.getList("Entries", Tag.TAG_COMPOUND);
        List<GenericStack> stacks = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                                      entryTag -> stacks.add(
                                              GenericStackSerializer.read(levelRegistryAccess, entryTag)));
        listTag = tag.getList("Crafts", Tag.TAG_COMPOUND);
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(listTag.size());
        NBTHelper.iterateCompoundList(listTag,
                                      entryTag -> {
                                          PackageOrder order = new PackageOrder(NBTHelper.readCompoundList(
                                                  entryTag.getList("Entries", Tag.TAG_COMPOUND),
                                                  t -> BigGenericStack.of(
                                                          GenericStackSerializer.read(levelRegistryAccess,
                                                                                      t)).asStack()));
                                          crafts.add(new PackageOrderWithCrafts.CraftingEntry(order, entryTag.getInt(
                                                  "Count")));
                                      });
        return new GenericOrder(stacks, crafts);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(stacks.size());
        for (GenericStack stack : stacks)
            GenericStackSerializer.write(stack, buffer);
        buffer.writeVarInt(crafts.size());
        for (PackageOrderWithCrafts.CraftingEntry entry : crafts) {
            PackageOrder pattern = entry.pattern();
            buffer.writeVarInt(pattern.stacks().size());
            for (BigItemStack stack : pattern.stacks()) {
                GenericStackSerializer.write(BigGenericStack.of(stack).get(), buffer);
            }
            buffer.writeVarInt(entry.count());
        }
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

    public static GenericOrder read(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<GenericStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            stacks.add(GenericStackSerializer.read(buffer));
        int craftSize = buffer.readVarInt();
        List<PackageOrderWithCrafts.CraftingEntry> crafts = new ArrayList<>(craftSize);
        for (int i = 0; i < craftSize; i++) {
            int patternSize = buffer.readVarInt();
            List<BigItemStack> pattern = new ArrayList<>(patternSize);
            for (int j = 0; j < patternSize; j++)
                pattern.add(BigGenericStack.of(GenericStackSerializer.read(buffer)).asStack());
            crafts.add(new PackageOrderWithCrafts.CraftingEntry(new PackageOrder(pattern), buffer.readVarInt()));
        }
        return new GenericOrder(stacks, crafts);
    }

    public static void set(HolderLookup.Provider levelRegistryAccess, ItemStack box, int orderId, int linkIndex,
                           boolean isFinalLink, int fragmentIndex,
                           boolean isFinal, @Nullable GenericOrder orderContext) {
        if (!CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE) {
            PackageItem.setOrder(box, orderId, linkIndex, isFinalLink, fragmentIndex, isFinal,
                                 orderContext == null ? null : orderContext.asCrafting());
            return;
        }
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

    public static GenericOrder of(HolderLookup.Provider levelRegistryAccess, ItemStack box) {
        if (!CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE) {
            PackageOrderWithCrafts orderContext = PackageItem.getOrderContext(box);
            return orderContext == null ? null : GenericOrder.of(orderContext);
        }
        CustomData data = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.contains("Fragment"))
            return null;
        CompoundTag frag = data.copyTag().getCompound("Fragment");
        if (!frag.contains("OrderContext"))
            return null;
        return GenericOrder.read(levelRegistryAccess, frag.getCompound("OrderContext"));
    }
}
