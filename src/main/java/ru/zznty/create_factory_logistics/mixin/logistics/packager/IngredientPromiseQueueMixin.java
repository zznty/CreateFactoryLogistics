package ru.zznty.create_factory_logistics.mixin.logistics.packager;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.packagerLink.RequestPromise;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.zznty.create_factory_logistics.logistics.panel.request.*;

import java.util.*;

@Mixin(RequestPromiseQueue.class)
public class IngredientPromiseQueueMixin implements IngredientPromiseQueue {
    @Unique
    private final Map<Fluid, List<RequestPromise>> createFactoryLogistics$promiseByFluid = new IdentityHashMap<>();

    @Shadow(remap = false)
    private Map<Item, List<RequestPromise>> promisesByItem;
    @Shadow(remap = false)
    private Runnable onChanged;

    @Shadow(remap = false)
    public void forceClear(ItemStack stack) {
    }

    @Shadow(remap = false)
    public int getTotalPromisedAndRemoveExpired(ItemStack stack, int expiryTime) {
        return 0;
    }

    @Shadow(remap = false)
    public void itemEnteredSystem(ItemStack stack, int amount) {
    }

    @Override
    public void add(BigIngredientStack stack) {
        add(new RequestPromise(stack.asStack()));
    }

    @Override
    public void forceClear(BoardIngredient ingredient) {
        if (ingredient instanceof ItemBoardIngredient itemIngredient) {
            forceClear(itemIngredient.stack());
        } else if (ingredient instanceof FluidBoardIngredient fluidIngredient) {
            List<RequestPromise> list = createFactoryLogistics$promiseByFluid.get(fluidIngredient.stack().getFluid());
            if (list == null)
                return;

            for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext(); ) {
                RequestPromise promise = iterator.next();

                BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;

                if (!stack.getIngredient().canStack(ingredient))
                    continue;
                iterator.remove();
                onChanged.run();
            }

            if (list.isEmpty())
                createFactoryLogistics$promiseByFluid.remove(fluidIngredient.stack().getFluid());
        }
    }

    @Override
    public int getTotalPromisedAndRemoveExpired(BoardIngredient ingredient, int expiryTime) {
        if (ingredient instanceof ItemBoardIngredient itemIngredient) {
            return getTotalPromisedAndRemoveExpired(itemIngredient.stack(), expiryTime);
        } else if (ingredient instanceof FluidBoardIngredient fluidIngredient) {
            int promised = 0;
            List<RequestPromise> list = createFactoryLogistics$promiseByFluid.get(fluidIngredient.stack().getFluid());
            if (list == null)
                return promised;

            for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext(); ) {
                RequestPromise promise = iterator.next();
                BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;
                if (!fluidIngredient.canStack(stack.getIngredient()))
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
        return 0;
    }

    @Override
    public void ingredientEnteredSystem(BoardIngredient ingredient) {
        if (ingredient instanceof ItemBoardIngredient itemIngredient) {
            itemEnteredSystem(itemIngredient.stack(), itemIngredient.amount());
        } else if (ingredient instanceof FluidBoardIngredient fluidIngredient) {
            List<RequestPromise> list = createFactoryLogistics$promiseByFluid.get(fluidIngredient.stack().getFluid());
            if (list == null)
                return;

            int amount = fluidIngredient.amount();

            for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext(); ) {
                RequestPromise requestPromise = iterator.next();
                BigIngredientStack stack = (BigIngredientStack) requestPromise.promisedStack;
                if (!fluidIngredient.canStack(stack.getIngredient()))
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

            if (list.isEmpty())
                createFactoryLogistics$promiseByFluid.remove(fluidIngredient.stack().getFluid());
        }
    }

    @Overwrite(remap = false)
    public void add(RequestPromise promise) {
        BigIngredientStack stack = (BigIngredientStack) promise.promisedStack;

        if (stack.getIngredient() instanceof FluidBoardIngredient fluidIngredient) {
            createFactoryLogistics$promiseByFluid.computeIfAbsent(fluidIngredient.stack().getFluid(), $ -> new LinkedList<>())
                    .add(promise);
        } else if (stack.getIngredient() instanceof ItemBoardIngredient itemIngredient) {
            promisesByItem.computeIfAbsent(itemIngredient.stack().getItem(), $ -> new LinkedList<>())
                    .add(promise);
        }

        if (stack.getIngredient() != BoardIngredient.EMPTY)
            onChanged.run();
    }

    @Inject(
            method = "tick",
            at = @At("RETURN"),
            remap = false
    )
    private void tickFluids(CallbackInfo ci) {
        createFactoryLogistics$promiseByFluid.forEach((key, list) -> list.forEach(RequestPromise::tick));
    }

    @Inject(
            method = "isEmpty",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void isFluidsEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && !createFactoryLogistics$promiseByFluid.isEmpty())
            cir.setReturnValue(false);
    }

    @Inject(
            method = "flatten",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
            ),
            remap = false
    )
    private void flattenFluids(boolean sorted, CallbackInfoReturnable<List<RequestPromise>> cir, @Local List<RequestPromise> all) {
        createFactoryLogistics$promiseByFluid.forEach((key, list) -> all.addAll(list));
    }
}
