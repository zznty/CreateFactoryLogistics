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
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemIngredientKey;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.*;

@Mixin(InventorySummary.class)
public class InventorySummaryMixin implements IngredientInventorySummary {
    @Unique
    private final Multimap<IngredientKey, BigIngredientStack> createFactoryLogistics$ingredients = HashMultimap.create();

    @Unique
    private List<BigIngredientStack> createFactoryLogistics$stacksByCount;

    @Shadow(remap = false)
    private int totalCount;

    @Shadow(remap = false)
    public int contributingLinks;

    @Overwrite(remap = false)
    public void add(ItemStack stack, int count) {
        if (stack.isEmpty() || count == 0) return;
        add(new BoardIngredient(IngredientKey.of(stack), count));
    }

    @Overwrite(remap = false)
    public int getCountOf(ItemStack stack) {
        return getCountOf(IngredientKey.of(stack));
    }

    @Overwrite(remap = false)
    public List<BigIngredientStack> getStacks() {
        // note: resulting list must be mutable
        List<BigIngredientStack> stacks = new ArrayList<>();
        for (Collection<BigIngredientStack> value : createFactoryLogistics$ingredients.asMap().values()) {
            Iterator<BigIngredientStack> iter = value.iterator();
            BigIngredientStack stack = BigIngredientStack.of(iter.next().ingredient());
            while (iter.hasNext()) {
                stack.setCount(stack.getCount() + iter.next().getCount());
            }
            stacks.add(stack);
        }
        return stacks;
    }

    @Override
    public List<BoardIngredient> get() {
        // note: resulting list must be mutable
        List<BoardIngredient> list = new ArrayList<>(createFactoryLogistics$ingredients.size());
        for (BigIngredientStack stack : createFactoryLogistics$ingredients.values()) {
            list.add(stack.ingredient());
        }
        return list;
    }

    @Overwrite(remap = false)
    public boolean erase(ItemStack stack) {
        return erase(IngredientKey.of(stack));
    }

    @Overwrite(remap = false)
    public void add(InventorySummary summary) {
        IngredientInventorySummary otherSummary = (IngredientInventorySummary) summary;
        for (BoardIngredient ingredient : otherSummary.get()) {
            add(ingredient);
        }
        contributingLinks += summary.contributingLinks;
    }

    @Overwrite(remap = false)
    public InventorySummary copy() {
        IngredientInventorySummary copy = (IngredientInventorySummary) new InventorySummary();
        copy.add(this);
        return (InventorySummary) copy;
    }

    @Overwrite(remap = false)
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("List", NBTHelper.writeCompoundList(createFactoryLogistics$ingredients.values(), stack -> stack.asStack().write()));
        return tag;
    }

    @Overwrite(remap = false)
    public static InventorySummary read(CompoundTag tag) {
        IngredientInventorySummary summary = (IngredientInventorySummary) new InventorySummary();

        ListTag listTag = tag.getList("List", Tag.TAG_COMPOUND);
        NBTHelper.iterateCompoundList(listTag, compoundTag -> summary.add(((BigIngredientStack) BigItemStack.read(compoundTag)).ingredient()));
        return (InventorySummary) summary;
    }

    @Override
    @Overwrite(remap = false)
    public boolean isEmpty() {
        return createFactoryLogistics$ingredients.isEmpty();
    }

    @Overwrite(remap = false)
    public List<BigIngredientStack> getStacksByCount() {
        if (createFactoryLogistics$stacksByCount == null) {
            createFactoryLogistics$stacksByCount = getStacks();
            createFactoryLogistics$stacksByCount.sort(BigIngredientStack.COMPARATOR);
        }
        return createFactoryLogistics$stacksByCount;
    }

    @Overwrite(remap = false)
    public Map<Item, List<BigIngredientStack>> getItemMap() {
        Map<Item, List<BigIngredientStack>> map = new IdentityHashMap<>();
        for (Map.Entry<IngredientKey, Collection<BigIngredientStack>> entry : createFactoryLogistics$ingredients.asMap().entrySet()) {
            if (entry.getKey() instanceof ItemIngredientKey itemKey)
                map.put(itemKey.stack().getItem(), new ArrayList<>(entry.getValue()));
        }
        return map;
    }

    @Overwrite(remap = false)
    public void add(BigItemStack stack) {
        add(((BigIngredientStack) stack).ingredient());
    }

    @Override
    public void add(BoardIngredient ingredient) {
        if (ingredient.isEmpty()) return;

        createFactoryLogistics$invalidate();

        if (totalCount < BigItemStack.INF)
            totalCount += ingredient.amount();

        Collection<BigIngredientStack> stacks = createFactoryLogistics$ingredients.get(ingredient.key().genericCopy());

        for (BigIngredientStack stack : stacks) {
            if (stack.ingredient().canStack(ingredient)) {
                if (!stack.isInfinite())
                    stack.setCount(stack.getCount() + ingredient.amount());
                return;
            }
        }

        stacks.add(BigIngredientStack.of(ingredient, ingredient.amount()));
    }

    @Override
    public void add(IngredientInventorySummary summary) {
        add((InventorySummary) summary);
    }

    @Override
    public int getCountOf(IngredientKey key) {
        int count = 0;
        for (BigIngredientStack stack : createFactoryLogistics$ingredients.get(key.genericCopy())) {
            if (stack.ingredient().canStack(key)) {
                if (count < BigItemStack.INF)
                    count += stack.getCount();
            }
        }

        return count;
    }

    @Override
    public int getCountOf(BigIngredientStack stack) {
        return getCountOf(stack.ingredient().key());
    }

    @Override
    public boolean erase(IngredientKey key) {
        Collection<BigIngredientStack> stacks = createFactoryLogistics$ingredients.get(key.genericCopy());
        if (stacks.isEmpty()) return false;

        for (Iterator<BigIngredientStack> iterator = stacks.iterator(); iterator.hasNext(); ) {
            BigIngredientStack existing = iterator.next();
            if (!existing.ingredient().canStack(key)) continue;
            totalCount -= existing.getCount();
            iterator.remove();
            createFactoryLogistics$invalidate();
            return true;
        }
        return false;
    }

    @Unique
    private void createFactoryLogistics$invalidate() {
        createFactoryLogistics$stacksByCount = null;
    }
}
