package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.*;

@Mixin(InventorySummary.class)
public class InventorySummaryMixin implements GenericInventorySummary {
    @Unique
    private final Multimap<GenericKey, BigGenericStack> createFactoryLogistics$stacks = HashMultimap.create();

    @Unique
    private List<BigGenericStack> createFactoryLogistics$stacksByCount;

    @Shadow(remap = false)
    private int totalCount;

    @Shadow(remap = false)
    public int contributingLinks;

    @Overwrite(remap = false)
    public void add(ItemStack stack, int count) {
        if (stack.isEmpty() || count == 0) return;
        add(GenericStack.wrap(stack).withAmount(count));
    }

    @Overwrite(remap = false)
    public int getCountOf(ItemStack stack) {
        return getCountOf(GenericStack.wrap(stack).key());
    }

    @Overwrite(remap = false)
    public List<BigGenericStack> getStacks() {
        // note: resulting list must be mutable
        List<BigGenericStack> stacks = new ArrayList<>();
        for (Collection<BigGenericStack> value : createFactoryLogistics$stacks.asMap().values()) {
            Iterator<BigGenericStack> iter = value.iterator();
            BigGenericStack stack = BigGenericStack.of(iter.next().get());
            while (iter.hasNext()) {
                stack.setAmount(stack.get().amount() + iter.next().get().amount());
            }
            stacks.add(stack);
        }
        return stacks;
    }

    @Override
    public List<GenericStack> get() {
        // note: resulting list must be mutable
        List<GenericStack> list = new ArrayList<>(createFactoryLogistics$stacks.size());
        for (BigGenericStack stack : createFactoryLogistics$stacks.values()) {
            list.add(stack.get());
        }
        return list;
    }

    @Override
    public Map<GenericKey, Collection<BigGenericStack>> getMap() {
        return createFactoryLogistics$stacks.asMap();
    }

    @Overwrite(remap = false)
    public boolean erase(ItemStack stack) {
        return erase(GenericStack.wrap(stack).key());
    }

    @Overwrite(remap = false)
    public void add(InventorySummary summary) {
        GenericInventorySummary otherSummary = GenericInventorySummary.of(summary);
        for (GenericStack stack : otherSummary.get()) {
            add(stack);
        }
        contributingLinks += summary.contributingLinks;
    }

    @Overwrite(remap = false)
    public InventorySummary copy() {
        GenericInventorySummary copy = GenericInventorySummary.empty();
        copy.add(this);
        return copy.asSummary();
    }

    @Overwrite(remap = false)
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("List", NBTHelper.writeCompoundList(createFactoryLogistics$stacks.values(),
                                                    stack -> stack.asStack().write()));
        return tag;
    }

    @Overwrite(remap = false)
    public static InventorySummary read(CompoundTag tag) {
        GenericInventorySummary summary = GenericInventorySummary.empty();

        ListTag listTag = tag.getList("List", Tag.TAG_COMPOUND);
        NBTHelper.iterateCompoundList(listTag, compoundTag -> summary.add(GenericStackSerializer.read(compoundTag)));
        return summary.asSummary();
    }

    @Override
    @Overwrite(remap = false)
    public boolean isEmpty() {
        return createFactoryLogistics$stacks.isEmpty();
    }

    @Overwrite(remap = false)
    public List<BigGenericStack> getStacksByCount() {
        if (createFactoryLogistics$stacksByCount == null) {
            createFactoryLogistics$stacksByCount = getStacks();
            createFactoryLogistics$stacksByCount.sort(BigGenericStack.COMPARATOR);
        }
        return createFactoryLogistics$stacksByCount;
    }

    @Overwrite(remap = false)
    public Map<Item, List<BigGenericStack>> getItemMap() {
        Map<Item, List<BigGenericStack>> map = new IdentityHashMap<>();
        for (Map.Entry<GenericKey, Collection<BigGenericStack>> entry : createFactoryLogistics$stacks.asMap().entrySet()) {
            if (entry.getKey() instanceof ItemKey itemKey)
                map.put(itemKey.stack().getItem(), new ArrayList<>(entry.getValue()));
        }
        return map;
    }

    @Overwrite(remap = false)
    public void add(BigItemStack stack) {
        add(BigGenericStack.of(stack).get());
    }

    @Override
    public void add(GenericStack stack) {
        if (stack.isEmpty()) return;

        createFactoryLogistics$invalidate();

        if (totalCount < BigItemStack.INF)
            totalCount += stack.amount();

        Collection<BigGenericStack> stacks = createFactoryLogistics$stacks.get(
                GenericContentExtender.registrationOf(stack.key()).provider().wrapGeneric(stack.key()));

        for (BigGenericStack bigStack : stacks) {
            if (bigStack.get().canStack(stack)) {
                if (bigStack.get().amount() != BigItemStack.INF)
                    bigStack.setAmount(bigStack.get().amount() + stack.amount());
                return;
            }
        }

        stacks.add(BigGenericStack.of(stack));
    }

    @Override
    public void add(GenericInventorySummary summary) {
        add(summary.asSummary());
    }

    @Override
    public int getCountOf(GenericKey key) {
        int count = 0;
        for (BigGenericStack stack : createFactoryLogistics$stacks.get(
                GenericContentExtender.registrationOf(key).provider().wrapGeneric(key))) {
            if (stack.get().canStack(key)) {
                if (count < BigItemStack.INF)
                    count += stack.get().amount();
            }
        }

        return count;
    }

    @Override
    public boolean erase(GenericKey key) {
        Collection<BigGenericStack> stacks = createFactoryLogistics$stacks.get(
                GenericContentExtender.registrationOf(key).provider().wrapGeneric(key));
        if (stacks.isEmpty()) return false;

        for (Iterator<BigGenericStack> iterator = stacks.iterator(); iterator.hasNext(); ) {
            BigGenericStack existing = iterator.next();
            if (!existing.get().canStack(key)) continue;
            totalCount -= existing.get().amount();
            iterator.remove();
            createFactoryLogistics$invalidate();
            return true;
        }
        return false;
    }

    @Override
    public InventorySummary asSummary() {
        return (InventorySummary) (Object) this;
    }

    @Unique
    private void createFactoryLogistics$invalidate() {
        createFactoryLogistics$stacksByCount = null;
    }
}
