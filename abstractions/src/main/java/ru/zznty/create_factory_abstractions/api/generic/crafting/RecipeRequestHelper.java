package ru.zznty.create_factory_abstractions.api.generic.crafting;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.createmod.catnip.data.Pair;
import net.minecraft.util.Mth;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericIngredient;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.CraftableGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;

import java.util.*;
import java.util.function.Function;

public final class RecipeRequestHelper {
    public static void requestCraftable(OrderProvider provider, CraftableGenericStack craftableStack,
                                        int requestedDifference) {
        boolean takeOrdersAway = requestedDifference < 0;
        if (takeOrdersAway)
            requestedDifference = Math.max(-craftableStack.get().amount(), requestedDifference);
        if (requestedDifference == 0)
            return;

        GenericInventorySummary availableItems = provider.stockSnapshot();
        Function<GenericStack, Integer> countModifier = ingredient -> {
            BigGenericStack ordered = provider.orderForStack(ingredient);
            return ordered == null ? 0 : -ordered.get().amount();
        };

        if (takeOrdersAway) {
            availableItems = GenericInventorySummary.empty();
            for (BigGenericStack ordered : provider.itemsToOrder()) {
                availableItems.add(ordered.get());
            }
            countModifier = ingredient -> 0;
        }

        Pair<Integer, Map<GenericIngredient, List<GenericStack>>> craftingResult =
                maxCraftable(provider, craftableStack, availableItems, countModifier,
                             takeOrdersAway ? -1 : 9 - provider.itemsToOrder().size());
        int outputCount = craftableStack.outputCount(provider.world());
        int adjustToRecipeAmount = Mth.ceil(Math.abs(requestedDifference) / (float) outputCount) * outputCount;
        int maxCraftable = Math.min(adjustToRecipeAmount, craftingResult.getFirst());

        if (maxCraftable == 0)
            return;

        craftableStack.setAmount(craftableStack.get().amount() + (takeOrdersAway ? -maxCraftable : maxCraftable));

        for (Map.Entry<GenericIngredient, List<GenericStack>> entry : craftingResult.getSecond().entrySet()) {
            int remaining = maxCraftable / outputCount * entry.getKey().amount();
            for (GenericStack stack : entry.getValue()) {
                if (remaining <= 0)
                    break;

                int toTransfer = Math.min(remaining, stack.amount());
                BigGenericStack order = provider.orderForStack(stack);

                if (takeOrdersAway) {
                    if (order != null) {
                        order.setAmount(order.get().amount() - toTransfer);
                        if (order.get().amount() <= 0)
                            provider.itemsToOrder().remove(order);
                    }
                } else {
                    if (order == null)
                        provider.itemsToOrder().add(order = BigGenericStack.of(stack.withAmount(0)));
                    order.setAmount(order.get().amount() + toTransfer);
                }

                remaining -= stack.amount();
            }
        }

        updateCraftableAmounts(provider);
    }

    public static boolean updateCraftableAmounts(OrderProvider provider) {
        InventorySummary usedItems = new InventorySummary();
        InventorySummary availableItems = new InventorySummary();

        GenericInventorySummary usedIngredients = GenericInventorySummary.of(usedItems);
        GenericInventorySummary availableIngredients = GenericInventorySummary.of(availableItems);

        for (BigGenericStack ordered : provider.itemsToOrder()) {
            availableIngredients.add(ordered.get());
        }

        for (CraftableGenericStack craftableStack : provider.recipesToOrder()) {
            /*if (craftableStack.ingredients().isEmpty()) {
                Pair<Integer, List<List<BigItemStack>>> craftingResult =
                        maxCraftable(craftableStack.asStack(), availableItems, stack -> -usedItems.getCountOf(stack),
                                     -1);
                int maxCraftable = craftingResult.getFirst();
                List<List<BigItemStack>> validEntriesByIngredient = craftingResult.getSecond();
                int outputCount = craftableStack.outputCount(provider.world());

                // Only tweak amounts downward
                craftableStack.setAmount(Math.min(craftableStack.get().amount(), maxCraftable));

                // Use ingredients up before checking next recipe
                for (List<BigItemStack> list : validEntriesByIngredient) {
                    int remaining = craftableStack.get().amount() / outputCount;
                    for (BigItemStack entry : list) {
                        if (remaining <= 0)
                            break;
                        usedItems.add(entry.stack, Math.min(remaining, entry.count));
                        remaining -= entry.count;
                    }
                }
            } else {*/
            Pair<Integer, Map<GenericIngredient, List<GenericStack>>> craftingResult =
                    maxCraftable(provider, craftableStack, availableIngredients,
                                 stack -> -usedIngredients.getCountOf(stack.key()), -1);
            int maxCraftable = craftingResult.getFirst();
            Map<GenericIngredient, List<GenericStack>> validEntriesByIngredient = craftingResult.getSecond();
            int outputCount = craftableStack.outputCount(provider.world());

            // Only tweak amounts downward
            craftableStack.setAmount(Math.min(craftableStack.get().amount(), maxCraftable));

            // Use ingredients up before checking next recipe
            for (Map.Entry<GenericIngredient, List<GenericStack>> entry : validEntriesByIngredient.entrySet()) {
                int remaining = craftableStack.get().amount() / outputCount * entry.getKey().amount();
                for (GenericStack stack : entry.getValue()) {
                    if (remaining <= 0)
                        break;
                    usedIngredients.add(stack.withAmount(Math.min(remaining, stack.amount())));
                    remaining -= stack.amount();
                }
            }
            /*}*/
        }

        for (BigGenericStack ordered : provider.itemsToOrder()) {
            if (usedIngredients.getCountOf(ordered.get().key()) != ordered.get().amount())
                return false;
        }
        return true;
    }

    private static Pair<Integer, Map<GenericIngredient, List<GenericStack>>> maxCraftable(
            OrderProvider provider,
            CraftableGenericStack cbis,
            GenericInventorySummary summary,
            Function<GenericStack, Integer> countModifier,
            int newTypeLimit) {
        List<GenericIngredient> ingredients = cbis.ingredients();
        // ingredient -> valid stacks
        Map<GenericIngredient, List<GenericStack>> validEntriesByIngredient = new HashMap<>();
        List<GenericStack> alreadyCreated = new ArrayList<>();

        for (GenericIngredient ingredient : ingredients) {
            if (ingredient.isEmpty())
                continue;
            List<GenericStack> valid = new ArrayList<>();
            for (Collection<BigGenericStack> list : summary.getMap().values())
                Entries:for (BigGenericStack entry : list) {
                    if (!ingredient.test(entry.get()))
                        continue;
                    for (GenericStack visitedStack : alreadyCreated) {
                        if (!visitedStack.canStack(entry.get()))
                            continue;
                        valid.add(visitedStack);
                        continue Entries;
                    }

                    GenericStack asBis = entry.get().withAmount(
                            summary.getCountOf(entry.get().key()) + countModifier.apply(entry.get()));
                    if (asBis.amount() > 0) {
                        valid.add(asBis);
                        alreadyCreated.add(asBis);
                    }
                }

            if (valid.isEmpty())
                return Pair.of(0, Map.of());

            valid.sort((bis1, bis2) -> -Integer.compare(summary.getCountOf(bis1.key()),
                                                        summary.getCountOf(bis2.key())));
            validEntriesByIngredient.put(ingredient, valid);
        }

        // Used new items may have to be trimmed
        if (newTypeLimit != -1) {
            int toRemove = (int) validEntriesByIngredient.values().stream()
                    .flatMap(Collection::stream)
                    .filter(entry -> provider.orderForStack(entry) == null)
                    .distinct()
                    .count() - newTypeLimit;

            for (int i = 0; i < toRemove; i++)
                removeLeastEssentialStack(provider, validEntriesByIngredient.values());
        }

        // Ingredients with shared items must divide counts
        validEntriesByIngredient = resolveIngredientAmounts(validEntriesByIngredient);

        // Determine the bottlenecking ingredient
        int minCount = Integer.MAX_VALUE;
        for (Map.Entry<GenericIngredient, List<GenericStack>> validEntry : validEntriesByIngredient.entrySet()) {
            int sum = 0;
            for (GenericStack entry : validEntry.getValue())
                sum += entry.amount() / validEntry.getKey().amount();
            minCount = Math.min(sum, minCount);
        }

        if (minCount == 0)
            return Pair.of(0, Map.of());

        int outputCount = cbis.outputCount(provider.world());
        return Pair.of(minCount * outputCount, validEntriesByIngredient);
    }

    private static void removeLeastEssentialStack(
            OrderProvider provider,
            Collection<List<GenericStack>> validIngredients) {
        List<GenericStack> longest = null;
        int most = 0;
        for (List<GenericStack> list : validIngredients) {
            int count = (int) list.stream()
                    .filter(entry -> provider.orderForStack(entry) == null)
                    .count();
            if (longest != null && count <= most)
                continue;
            longest = list;
            most = count;
        }

        if (longest == null || longest.isEmpty())
            return;

        GenericStack chosen = null;
        for (int i = 0; i < longest.size(); i++) {
            GenericStack entry = longest.get(longest.size() - 1 - i);
            if (provider.orderForStack(entry) != null)
                continue;
            chosen = entry;
            break;
        }

        for (List<GenericStack> list : validIngredients)
            list.remove(chosen);
    }

    private static Map<GenericIngredient, List<GenericStack>> resolveIngredientAmounts(
            Map<GenericIngredient, List<GenericStack>> validIngredients) {
        Map<GenericIngredient, List<GenericStack>> resolvedIngredients = new HashMap<>();
        for (GenericIngredient ingredient : validIngredients.keySet()) {
            resolvedIngredients.put(ingredient, new ArrayList<>());
        }

        boolean everythingTaken = false;
        while (!everythingTaken) {
            everythingTaken = true;
            Ingredients:
            for (GenericIngredient ingredient : validIngredients.keySet()) {
                List<GenericStack> list = validIngredients.get(ingredient);
                List<GenericStack> resolvedList = resolvedIngredients.get(ingredient);
                for (int j = 0; j < list.size(); j++) {
                    GenericStack stack = list.get(j);
                    if (stack.amount() <= 0)
                        continue;

                    list.set(j, stack.withAmount(stack.amount() - ingredient.amount()));
                    everythingTaken = false;

                    for (int k = 0; k < resolvedList.size(); k++) {
                        GenericStack resolvedItemStack = resolvedList.get(k);
                        if (resolvedItemStack == stack) {
                            resolvedList.set(k, resolvedItemStack.withAmount(
                                    resolvedItemStack.amount() + ingredient.amount()));
                            continue Ingredients;
                        }
                    }

                    resolvedList.add(stack.withAmount(ingredient.amount()));
                    continue Ingredients;
                }
            }
        }

        return resolvedIngredients;
    }
}
