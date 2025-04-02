package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientPromiseQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(RequestPromiseQueue.class)
public class IngredientPromiseQueueMixin implements IngredientPromiseQueue {
    @Unique
    private final Multimap<IngredientKey, RequestPromise> createFactoryLogistics$promises = HashMultimap.create();

    @Shadow(remap = false)
    private Runnable onChanged;

    @Overwrite(remap = false)
    public void forceClear(ItemStack stack) {
        forceClear(new BoardIngredient(IngredientKey.of(stack), 1));
    }

    @Overwrite(remap = false)
    public int getTotalPromisedAndRemoveExpired(ItemStack stack, int expiryTime) {
        return getTotalPromisedAndRemoveExpired(new BoardIngredient(IngredientKey.of(stack), 1), expiryTime);
    }

    @Overwrite(remap = false)
    public void itemEnteredSystem(ItemStack stack, int amount) {
        ingredientEnteredSystem(new BoardIngredient(IngredientKey.of(stack), amount));
    }

    @Override
    public void add(BigIngredientStack stack) {
        add(new RequestPromise(stack.asStack()));
    }

    @Override
    public void forceClear(BoardIngredient ingredient) {
        Collection<RequestPromise> promises = createFactoryLogistics$promises.get(ingredient.key().genericCopy());
        if (promises.isEmpty())
            return;

        for (Iterator<RequestPromise> iterator = promises.iterator(); iterator.hasNext(); ) {
            RequestPromise promise = iterator.next();

            BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;

            if (!stack.ingredient().canStack(ingredient))
                continue;
            iterator.remove();
            onChanged.run();
        }
    }

    @Override
    public int getTotalPromisedAndRemoveExpired(BoardIngredient ingredient, int expiryTime) {
        int promised = 0;
        Collection<RequestPromise> promises = createFactoryLogistics$promises.get(ingredient.key().genericCopy());
        if (promises.isEmpty())
            return promised;

        for (Iterator<RequestPromise> iterator = promises.iterator(); iterator.hasNext(); ) {
            RequestPromise promise = iterator.next();
            BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;
            if (!ingredient.canStack(stack.ingredient()))
                continue;
            if (expiryTime != -1 && promise.ticksExisted >= expiryTime) {
                iterator.remove();
                onChanged.run();
                continue;
            }

            promised += promise.promisedStack.count;
        }
        return promised;
    }

    @Override
    public void ingredientEnteredSystem(BoardIngredient ingredient) {
        Collection<RequestPromise> promises = createFactoryLogistics$promises.get(ingredient.key().genericCopy());
        if (promises.isEmpty())
            return;

        int amount = ingredient.amount();

        for (Iterator<RequestPromise> iterator = promises.iterator(); iterator.hasNext(); ) {
            RequestPromise requestPromise = iterator.next();
            BigIngredientStack stack = (BigIngredientStack) requestPromise.promisedStack;
            if (!ingredient.canStack(stack.ingredient()))
                continue;

            int toSubtract = Math.min(amount, stack.getCount());
            amount -= toSubtract;
            stack.setCount(stack.getCount() - toSubtract);

            if (stack.getCount() <= 0) {
                iterator.remove();
                onChanged.run();
            }
            if (amount <= 0)
                break;
        }
    }

    @Overwrite(remap = false)
    public void add(RequestPromise promise) {
        BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;

        createFactoryLogistics$promises.put(stack.ingredient().key().genericCopy(), promise);

        if (!stack.ingredient().isEmpty())
            onChanged.run();
    }

    @Overwrite(remap = false)
    public void tick() {
        createFactoryLogistics$promises.forEach((key, promise) -> promise.tick());
    }

    @Overwrite(remap = false)
    public boolean isEmpty() {
        return createFactoryLogistics$promises.isEmpty();
    }

    @Overwrite(remap = false)
    public List<RequestPromise> flatten(boolean sorted) {
        List<RequestPromise> all = new ArrayList<>(createFactoryLogistics$promises.values());
        if (sorted)
            all.sort(RequestPromise.ageComparator());
        return all;
    }
}
