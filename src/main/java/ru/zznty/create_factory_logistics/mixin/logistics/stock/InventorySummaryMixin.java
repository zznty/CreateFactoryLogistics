package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
import ru.zznty.create_factory_logistics.logistics.panel.request.LogisticalStockResponsePacket;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.*;

@Mixin(InventorySummary.class)
public class InventorySummaryMixin implements IngredientInventorySummary {
    @Unique
    private final Multimap<IngredientKey, BigIngredientStack> createFactoryLogistics$ingredients = HashMultimap.create();

    @Unique
    private List<BigIngredientStack> createFactoryLogistics$stacksByCount;

    @Shadow
    private int totalCount;

    @Shadow
    public int contributingLinks;

    @Overwrite
    public void add(ItemStack stack, int count) {
        if (stack.isEmpty() || count == 0) return;
        add(new BoardIngredient(IngredientKey.of(stack), count));
    }

    @Overwrite
    public int getCountOf(ItemStack stack) {
        return getCountOf(IngredientKey.of(stack));
    }

    @Overwrite
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

    @Overwrite
    public boolean erase(ItemStack stack) {
        return erase(IngredientKey.of(stack));
    }

    @Overwrite
    public void add(InventorySummary summary) {
        IngredientInventorySummary otherSummary = (IngredientInventorySummary) summary;
        for (BoardIngredient ingredient : otherSummary.get()) {
            add(ingredient);
        }
        contributingLinks += summary.contributingLinks;
    }

    @Overwrite
    public InventorySummary copy() {
        IngredientInventorySummary copy = (IngredientInventorySummary) new InventorySummary();
        copy.add(this);
        return (InventorySummary) copy;
    }

    @Override
    public CompoundTag write(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("List", NBTHelper.writeCompoundList(createFactoryLogistics$ingredients.values(), stack -> {
            CompoundTag compoundTag = new CompoundTag();
            stack.ingredient().write(registries, compoundTag);
            return compoundTag;
        }));
        return tag;
    }

    @Override
    @Overwrite
    public boolean isEmpty() {
        return createFactoryLogistics$ingredients.isEmpty();
    }

    @Overwrite
    public List<BigIngredientStack> getStacksByCount() {
        if (createFactoryLogistics$stacksByCount == null) {
            createFactoryLogistics$stacksByCount = getStacks();
            createFactoryLogistics$stacksByCount.sort(BigIngredientStack.COMPARATOR);
        }
        return createFactoryLogistics$stacksByCount;
    }

    @Overwrite
    public Map<Item, List<BigIngredientStack>> getItemMap() {
        Map<Item, List<BigIngredientStack>> map = new IdentityHashMap<>();
        for (Map.Entry<IngredientKey, Collection<BigIngredientStack>> entry : createFactoryLogistics$ingredients.asMap().entrySet()) {
            if (entry.getKey() instanceof ItemIngredientKey itemKey)
                map.put(itemKey.stack().getItem(), new ArrayList<>(entry.getValue()));
        }
        return map;
    }

    @Overwrite
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

    @Overwrite
    public void divideAndSendTo(ServerPlayer player, BlockPos pos) {
        List<BigIngredientStack> stacks = getStacksByCount();
        int remaining = stacks.size();

        List<BigItemStack> currentList = null;

        if (stacks.isEmpty())
            CatnipServices.NETWORK.sendToClient(player, new LogisticalStockResponsePacket(true, pos, Collections.emptyList()));

        for (BigIngredientStack entry : stacks) {
            if (currentList == null)
                currentList = new ArrayList<>(Math.min(100, remaining));

            currentList.add(entry.asStack());
            remaining--;

            if (remaining == 0)
                break;
            if (currentList.size() < 100)
                continue;

            CatnipServices.NETWORK.sendToClient(player, new LogisticalStockResponsePacket(false, pos, currentList));
            currentList = null;
        }

        if (currentList != null)
            CatnipServices.NETWORK.sendToClient(player, new LogisticalStockResponsePacket(true, pos, currentList));
    }

    @Unique
    private void createFactoryLogistics$invalidate() {
        createFactoryLogistics$stacksByCount = null;
    }
}
