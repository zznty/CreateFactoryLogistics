package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.panel.request.*;
import ru.zznty.create_factory_logistics.logistics.stock.IIngredientInventorySummary;

import java.util.*;

@Mixin(InventorySummary.class)
public class InventorySummaryMixin implements IIngredientInventorySummary {
    @Unique
    private final Multimap<IngredientKey, BigIngredientStack> createFactoryLogistics$ingredients = HashMultimap.create();

    @Shadow(remap = false)
    private int totalCount;

    @Shadow(remap = false)
    public int contributingLinks;

    @Overwrite(remap = false)
    public void add(ItemStack stack, int count) {
        if (stack.isEmpty() || count == 0) return;
        add(new ItemBoardIngredient(stack, count), count);
    }

    @Overwrite(remap = false)
    public int getCountOf(ItemStack stack) {
        return getCountOf(BigIngredientStack.of(new ItemBoardIngredient(stack, 1)));
    }

    @Overwrite(remap = false)
    public List<BigIngredientStack> getStacks() {
        // note: resulting list must be mutable
        return new ArrayList<>(createFactoryLogistics$ingredients.values());
    }

    @Overwrite(remap = false)
    public boolean erase(ItemStack stack) {
        return erase(new ItemBoardIngredient(stack, 1));
    }

    @Overwrite(remap = false)
    public void add(InventorySummary summary) {
        IIngredientInventorySummary otherSummary = (IIngredientInventorySummary) summary;
        for (BigIngredientStack stack : otherSummary.getStacks()) {
            add(stack);
        }
        contributingLinks += summary.contributingLinks;
    }

    @Overwrite(remap = false)
    public InventorySummary copy() {
        IIngredientInventorySummary copy = (IIngredientInventorySummary) new InventorySummary();
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
        IIngredientInventorySummary summary = (IIngredientInventorySummary) new InventorySummary();

        ListTag listTag = tag.getList("List", Tag.TAG_COMPOUND);
        NBTHelper.iterateCompoundList(listTag, compoundTag -> summary.add((BigIngredientStack) BigItemStack.read(compoundTag)));
        return (InventorySummary) summary;
    }

    @Override
    @Overwrite(remap = false)
    public boolean isEmpty() {
        return createFactoryLogistics$ingredients.isEmpty();
    }

    @Overwrite(remap = false)
    public List<BigIngredientStack> getStacksByCount() {
        List<BigIngredientStack> list = new ArrayList<>(createFactoryLogistics$ingredients.values());
        list.sort(Comparator.comparingInt(BigIngredientStack::getCount));
        return list;
    }

    @Overwrite(remap = false)
    public void add(BigItemStack stack) {
        add((BigIngredientStack) stack);
    }

    @Override
    public void add(BigIngredientStack stack) {
        add(stack.getIngredient(), stack.getCount());
    }

    @Override
    public void add(BoardIngredient ingredient, int count) {
        if (ingredient == BoardIngredient.EMPTY) return;

        if (totalCount < BigItemStack.INF)
            totalCount += count;

        Collection<BigIngredientStack> stacks = createFactoryLogistics$ingredients.get(ingredient.key());

        for (BigIngredientStack stack : stacks) {
            if (stack.getIngredient().canStack(ingredient)) {
                if (!stack.isInfinite())
                    stack.setCount(stack.getCount() + count);
                return;
            }
        }

        stacks.add(BigIngredientStack.of(ingredient, count));
    }

    @Override
    public void add(FluidStack stack) {
        add(stack, stack.getAmount());
    }

    @Override
    public void add(FluidStack stack, int amount) {
        if (stack.isEmpty() || amount == 0) return;
        add(new FluidBoardIngredient(stack, amount), amount);
    }

    @Override
    public void add(IIngredientInventorySummary summary) {
        add((InventorySummary) summary);
    }

    @Override
    public int getCountOf(BoardIngredient ingredient) {
        if (ingredient == BoardIngredient.EMPTY) return 0;

        int count = 0;
        for (BigIngredientStack stack : createFactoryLogistics$ingredients.get(ingredient.key())) {
            if (stack.getIngredient().canStack(ingredient)) {
                if (count < BigItemStack.INF)
                    count += stack.getCount();
            }
        }

        return count;
    }

    @Override
    public int getCountOf(BigIngredientStack stack) {
        return getCountOf(stack.getIngredient());
    }

    @Override
    public boolean erase(BoardIngredient ingredient) {
        if (ingredient == BoardIngredient.EMPTY) return false;

        Collection<BigIngredientStack> stacks = createFactoryLogistics$ingredients.get(ingredient.key());
        if (stacks.isEmpty()) return false;

        for (Iterator<BigIngredientStack> iterator = stacks.iterator(); iterator.hasNext(); ) {
            BigIngredientStack existing = iterator.next();
            if (!existing.getIngredient().canStack(ingredient)) continue;
            totalCount -= existing.getCount();
            iterator.remove();
            return true;
        }
        return false;
    }
}
