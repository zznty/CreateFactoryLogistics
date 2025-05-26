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
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericPromiseQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(RequestPromiseQueue.class)
public class GenericPromiseQueueMixin implements GenericPromiseQueue {
    @Unique
    private final Multimap<GenericKey, RequestPromise> createFactoryLogistics$promises = HashMultimap.create();

    @Shadow(remap = false)
    private Runnable onChanged;

    @Overwrite(remap = false)
    public void forceClear(ItemStack stack) {
        forceClear(GenericStack.wrap(stack));
    }

    @Overwrite(remap = false)
    public int getTotalPromisedAndRemoveExpired(ItemStack stack, int expiryTime) {
        return getTotalPromisedAndRemoveExpired(GenericStack.wrap(stack), expiryTime);
    }

    @Overwrite(remap = false)
    public void itemEnteredSystem(ItemStack stack, int amount) {
        stackEnteredSystem(GenericStack.wrap(stack).withAmount(amount));
    }

    @Override
    public void add(GenericStack stack) {
        add(new RequestPromise(BigGenericStack.of(stack).asStack()));
    }

    @Override
    public void forceClear(GenericStack stack) {
        Collection<RequestPromise> promises = createFactoryLogistics$promises.get(
                GenericContentExtender.registrationOf(stack.key()).provider().wrapGeneric(stack.key()));
        if (promises.isEmpty())
            return;

        for (Iterator<RequestPromise> iterator = promises.iterator(); iterator.hasNext(); ) {
            RequestPromise promise = iterator.next();

            BigGenericStack promisedStack = BigGenericStack.of(promise.promisedStack);

            if (!stack.canStack(promisedStack.get()))
                continue;
            iterator.remove();
            onChanged.run();
        }
    }

    @Override
    public int getTotalPromisedAndRemoveExpired(GenericStack stack, int expiryTime) {
        int promised = 0;
        Collection<RequestPromise> promises = createFactoryLogistics$promises.get(
                GenericContentExtender.registrationOf(stack.key()).provider().wrapGeneric(stack.key()));
        if (promises.isEmpty())
            return promised;

        for (Iterator<RequestPromise> iterator = promises.iterator(); iterator.hasNext(); ) {
            RequestPromise promise = iterator.next();
            BigGenericStack promisedStack = BigGenericStack.of(promise.promisedStack);
            if (!stack.canStack(promisedStack.get()))
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
    public void stackEnteredSystem(GenericStack stack) {
        Collection<RequestPromise> promises = createFactoryLogistics$promises.get(
                GenericContentExtender.registrationOf(stack.key()).provider().wrapGeneric(stack.key()));
        if (promises.isEmpty())
            return;

        int amount = stack.amount();

        for (Iterator<RequestPromise> iterator = promises.iterator(); iterator.hasNext(); ) {
            RequestPromise requestPromise = iterator.next();
            BigGenericStack promisedStack = BigGenericStack.of(requestPromise.promisedStack);

            if (!stack.canStack(promisedStack.get()))
                continue;

            int toSubtract = Math.min(amount, promisedStack.get().amount());
            amount -= toSubtract;
            promisedStack.setAmount(promisedStack.get().amount() - toSubtract);

            if (promisedStack.get().amount() <= 0) {
                iterator.remove();
                onChanged.run();
            }
            if (amount <= 0)
                break;
        }
    }

    @Overwrite(remap = false)
    public void add(RequestPromise promise) {
        BigGenericStack promisedStack = BigGenericStack.of(promise.promisedStack);

        createFactoryLogistics$promises.put(
                GenericContentExtender.registrationOf(promisedStack.get().key()).provider().wrapGeneric(
                        promisedStack.get().key()), promise);

        if (!promisedStack.get().isEmpty())
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
