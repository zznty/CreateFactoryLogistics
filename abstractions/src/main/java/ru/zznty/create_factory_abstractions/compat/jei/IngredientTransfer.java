package ru.zznty.create_factory_abstractions.compat.jei;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

import java.util.*;
import java.util.stream.Collectors;

public final class IngredientTransfer {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Optional<GenericStack> tryConvert(IIngredientManager ingredientManager,
                                                    ITypedIngredient<?> typedIngredient) {
        String typeUid = typedIngredient.getType().getUid();
        for (GenericKeyRegistration registration : GenericContentExtender.REGISTRATIONS.values()) {
            if (registration.provider().ingredientTypeUid().equals(typeUid)) {
                IIngredientHelper ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
                return Optional.of(new GenericStack(registration.provider().wrap(typedIngredient.getIngredient()),
                                                    (int) ingredientHelper.getAmount(typedIngredient.getIngredient())));
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TransferOperationsResult getRecipeTransferOperations(
            IIngredientManager ingredientManager,
            List<GenericStack> availableIngredients,
            List<IRecipeSlotView> requiredStacks,
            List<Slot> craftingSlots
    ) {
        TransferOperationsResult transferOperations = TransferOperationsResult.create();

        // Find groups of slots for each recipe input, so each ingredient knows list of slots it can take item from
        // and also split them between "equal" groups
        Map<IRecipeSlotView, Map<MutableObject<GenericStack>, ArrayList<PhantomSlotState>>> relevantSlots = new IdentityHashMap<>();

        Map<IRecipeSlotView, Map<Object, ITypedIngredient<?>>> slotUidCache = new IdentityHashMap<>();
        List<IRecipeSlotView> nonEmptyRequiredStacks = requiredStacks.stream()
                .filter(r -> !r.isEmpty())
                .toList();

        List<MutableObject<GenericStack>> availableStacks = availableIngredients.stream().map(
                MutableObject::new).toList();

        for (int i = 0; i < availableStacks.size(); i++) {
            MutableObject<GenericStack> availableStack = availableStacks.get(i);
            Object slotItemStackUid = getStackUid(ingredientManager, availableStack.getValue(), UidContext.Ingredient);

            for (IRecipeSlotView ingredient : nonEmptyRequiredStacks) {
                Map<Object, ITypedIngredient<?>> ingredientUids = slotUidCache.computeIfAbsent(ingredient, s ->
                        s.getAllIngredients()
                                .map(typedIngredient -> {
                                    IIngredientHelper ingredientHelper1 = ingredientManager.getIngredientHelper(
                                            typedIngredient.getType());
                                    return Pair.of(ingredientHelper1.getUid(typedIngredient.getIngredient(),
                                                                            UidContext.Ingredient), typedIngredient);
                                })
                                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond))
                );

                ITypedIngredient<?> selectedIngredient = ingredientUids.get(slotItemStackUid);
                if (selectedIngredient != null) {
                    relevantSlots
                            .computeIfAbsent(ingredient,
                                             it -> new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
                                                 @Override
                                                 public int hashCode(MutableObject<GenericStack> o) {
                                                     return o.getValue().key().hashCode();
                                                 }

                                                 @Override
                                                 public boolean equals(MutableObject<GenericStack> a,
                                                                       MutableObject<GenericStack> b) {
                                                     if (a == null || b == null) return false;
                                                     return getStackUid(ingredientManager, a.getValue(),
                                                                        UidContext.Ingredient)
                                                             .equals(getStackUid(ingredientManager, b.getValue(),
                                                                                 UidContext.Ingredient));
                                                 }
                                             }))
                            .computeIfAbsent(availableStack, it -> new ArrayList<>())
                            .add(new PhantomSlotState(i, availableStack, selectedIngredient));
                }
            }
        }

        // Now we have Ingredient -> (type -> slots) list
        // But it is not sorted
        // So we construct a List containing Ingredient -> List<Lists of slots>
        // Then we sort each List so children List of slots so that List with Slots which contain
        // the most items appear at top (this is outer sort)

        // After we have done outer sort, we need to do inner sort, that is, sort lists containing slots themselves
        // so that slots with lesser items appear at top

        // We need to get following structure:
        // Ingredient1 -> listOf(MostItems(LeastItemsInSlot, MoreItemsInSlot, ...), LesserItems(), ...)

        Map<IRecipeSlotView, ArrayList<PhantomSlotStateList>> bestMatches = new Object2ObjectArrayMap<>();

        for (Map.Entry<IRecipeSlotView, Map<MutableObject<GenericStack>, ArrayList<PhantomSlotState>>> entry : relevantSlots.entrySet()) {
            ArrayList<PhantomSlotStateList> countedAndSorted = new ArrayList<>();

            for (Map.Entry<MutableObject<GenericStack>, ArrayList<PhantomSlotState>> foundSlots : entry.getValue().entrySet()) {
                // Ascending sort
                // if counts are equal, push slots with lesser index to top
                foundSlots.getValue().sort((o1, o2) -> {
                    int compare = Integer.compare(o1.stack.getValue().amount(), o2.stack.getValue().amount());

                    if (compare == 0) {
                        return Integer.compare(o1.slot, o2.slot);
                    }

                    return compare;
                });

                countedAndSorted.add(new PhantomSlotStateList(foundSlots.getValue()));
            }

            // Descending sort
            // if counts are equal, push groups with the lowest slot index to the top
            countedAndSorted.sort((o1, o2) -> {
                int compare = Long.compare(o2.totalAmount, o1.totalAmount);

                if (compare == 0) {
                    return Integer.compare(
                            o1.stateList.stream().mapToInt(it -> it.slot).min().orElse(0),
                            o2.stateList.stream().mapToInt(it -> it.slot).min().orElse(0)
                    );
                }

                return compare;
            });

            bestMatches.put(entry.getKey(), countedAndSorted);
        }

        for (int i = 0; i < requiredStacks.size(); i++) {
            IRecipeSlotView requiredStack = requiredStacks.get(i);

            if (requiredStack.isEmpty()) {
                continue;
            }

            Slot craftingSlot = craftingSlots.get(i);

            PhantomSlotState matching = null;
            List<PhantomSlotStateList> matches = bestMatches.get(requiredStack);
            if (matches != null) {
                for (PhantomSlotStateList phantomSlotStateList : matches) {
                    PhantomSlotState first = phantomSlotStateList.getFirstNonEmpty();
                    if (first != null) {
                        matching = first;
                        break;
                    }
                }
            }

            if (matching == null) {
                transferOperations.missingItems().add(requiredStack);
            } else {
                matching.stack.setValue(matching.stack.getValue().withAmount(matching.stack.getValue().amount() - 1));
                transferOperations.results().add(
                        new TransferOperation(matching.slot, craftingSlot.index, matching.typedIngredient));
            }
        }

        return transferOperations;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object getStackUid(IIngredientManager ingredientManager, GenericStack availableStack,
                                      UidContext context) {
        GenericKeyProvider<GenericKey> provider = GenericContentExtender.registrationOf(
                availableStack.key()).provider();

        IIngredientHelper ingredientHelper = ingredientManager.getIngredientHelper(
                ingredientManager.getIngredientTypeForUid(
                        provider.ingredientTypeUid()).orElseThrow());
        
        // inlining of variable breaks overload resolution
        Object value = provider.unwrap(availableStack.key());
        return ingredientHelper.getUid(value, context);
    }

    private record PhantomSlotState(int slot, MutableObject<GenericStack> stack,
                                    ITypedIngredient<?> typedIngredient) {
    }

    private record PhantomSlotStateList(List<PhantomSlotState> stateList, long totalAmount) {
        public PhantomSlotStateList(List<PhantomSlotState> states) {
            this(states, states.stream().mapToLong(it -> it.stack.getValue().amount()).sum());
        }

        @Nullable
        public PhantomSlotState getFirstNonEmpty() {
            for (PhantomSlotState state : this.stateList) {
                if (!state.stack.getValue().isEmpty()) {
                    return state;
                }
            }
            return null;
        }
    }
}

;
