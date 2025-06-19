package ru.zznty.create_factory_abstractions.api.generic.search;

import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class GenericSearch {
    public static SearchResult search(CategoriesProvider provider, String valueWithPrefix, int rowHeight, int cols) {
        List<CategoryEntry> categories = new ArrayList<>();
        for (int i = 0; i < provider.categories().size(); i++) {
            ItemStack stack = provider.categories().get(i);
            String name = stack.isEmpty() ? "" : stack.getHoverName().getString();
            CategoryEntry entry = new CategoryEntry(i, name, new MutableInt(),
                                                    new MutableBoolean(provider.hiddenCategories().contains(i)));
            categories.add(entry);
        }

        CategoryEntry unsorted = new CategoryEntry(-1,
                                                   CreateLang.translate(
                                                                   "gui.stock_keeper.unsorted_category")
                                                           .string(),
                                                   new MutableInt(),
                                                   new MutableBoolean(provider.hiddenCategories().contains(-1)));
        categories.add(unsorted);

        boolean anyItemsInCategory = false;
        List<List<BigGenericStack>> displayedItems;

        // Nothing is being filtered out
        if (valueWithPrefix.isBlank()) {
            displayedItems = new ArrayList<>(provider.currentItemSource());

            int categoryY = 0;
            for (int categoryIndex = 0; categoryIndex < provider.currentItemSource().size(); categoryIndex++) {
                categories.get(categoryIndex).y.setValue(categoryY);
                List<BigGenericStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
                if (displayedItemsInCategory.isEmpty())
                    continue;
                if (categoryIndex < provider.currentItemSource().size() - 1)
                    anyItemsInCategory = true;

                categoryY += rowHeight;
                if (categories.get(categoryIndex).hidden.isFalse())
                    categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
            }

            if (!anyItemsInCategory)
                categories.clear();

            return new SearchResult(categories, displayedItems);
        }

        // Filter by search string
        boolean modSearch = false;
        boolean tagSearch = false;
        if ((modSearch = valueWithPrefix.startsWith("@")) || (tagSearch = valueWithPrefix.startsWith("#")))
            valueWithPrefix = valueWithPrefix.substring(1);
        final String value = valueWithPrefix.toLowerCase(Locale.ROOT);

        displayedItems = new ArrayList<>();
        provider.currentItemSource().forEach($ -> displayedItems.add(new ArrayList<>()));

        int categoryY = 0;
        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            List<BigGenericStack> category = provider.currentItemSource().get(categoryIndex);
            categories.get(categoryIndex).y.setValue(categoryY);

            if (displayedItems.size() <= categoryIndex)
                break;

            List<BigGenericStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
            for (BigGenericStack entry : category) {
                GenericKeyRegistration registration = GenericContentExtender.registrationOf(entry.get().key());
                Optional<ResourceKey<Object>> key = registration.provider().resourceKey(entry.get().key());

                if (modSearch) {
                    if (key.isPresent() && key.get().location()
                            .getNamespace()
                            .contains(value)) {
                        displayedItemsInCategory.add(entry);
                    }
                    continue;
                }

                if (tagSearch) {
                    if (key.isPresent()) {
                        Optional<? extends Holder.Reference<?>> reverseTag = BuiltInRegistries.REGISTRY.get(
                                        key.get().registry())
                                .getHolder(key.get().location());
                        if (reverseTag.isPresent() && reverseTag.get().tags()
                                .anyMatch(tag -> tag.location()
                                        .toString()
                                        .contains(value)))
                            displayedItemsInCategory.add(entry);
                    }
                    continue;
                }

                if (registration.clientProvider().guiHandler().nameBuilder(entry.get().key())
                        .string()
                        .toLowerCase(Locale.ROOT)
                        .contains(value)
                        || key.isPresent() && key
                        .get().location().getPath()
                        .contains(value)) {
                    displayedItemsInCategory.add(entry);
                }
            }

            if (displayedItemsInCategory.isEmpty())
                continue;
            if (categoryIndex < provider.currentItemSource().size() - 1)
                anyItemsInCategory = true;

            categoryY += rowHeight;

            if (categories.get(categoryIndex).hidden.isFalse())
                categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
        }

        if (!anyItemsInCategory)
            categories.clear();

        return new SearchResult(categories, displayedItems);
    }

    public record CategoryEntry(int targetCategory, String name, MutableInt y, MutableBoolean hidden) {
    }

    public record SearchResult(List<CategoryEntry> categories, List<List<BigGenericStack>> displayedItems) {
    }
}
