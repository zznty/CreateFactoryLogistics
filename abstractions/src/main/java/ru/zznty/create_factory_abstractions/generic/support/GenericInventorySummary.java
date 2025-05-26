package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface GenericInventorySummary {
    void add(GenericStack stack);

    void add(GenericInventorySummary summary);

    int getCountOf(GenericKey key);

    List<GenericStack> get();

    Map<GenericKey, Collection<BigGenericStack>> getMap();

    boolean isEmpty();

    boolean erase(GenericKey key);

    InventorySummary asSummary();

    static GenericInventorySummary empty() {
        return of(new InventorySummary());
    }

    static GenericInventorySummary of(InventorySummary summary) {
        if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return Objects.requireNonNull((GenericInventorySummary) summary);

        return new GenericInventorySummary() {
            private final InventorySummary inventorySummary = summary;

            @Override
            public void add(GenericStack stack) {
                if (stack.key() instanceof ItemKey key)
                    inventorySummary.add(key.stack(), stack.amount());
            }

            @Override
            public void add(GenericInventorySummary summary) {
                inventorySummary.add(summary.asSummary());
            }

            @Override
            public int getCountOf(GenericKey key) {
                if (key instanceof ItemKey itemKey)
                    return inventorySummary.getCountOf(itemKey.stack());
                return 0;
            }

            @Override
            public List<GenericStack> get() {
                return inventorySummary.getStacks().stream().map(
                        s -> GenericStack.wrap(s.stack).withAmount(s.count)).toList();
            }

            @Override
            public Map<GenericKey, Collection<BigGenericStack>> getMap() {
                //noinspection unchecked
                return Map.ofEntries(inventorySummary.getItemMap().entrySet().stream().map(e ->
                                                                                                   Map.entry(
                                                                                                           new ItemKey(
                                                                                                                   e.getKey().getDefaultInstance()),
                                                                                                           e.getValue().stream().map(
                                                                                                                   BigGenericStack::of).toList()))
                                             .toArray(Map.Entry[]::new));
            }

            @Override
            public boolean isEmpty() {
                return inventorySummary.isEmpty();
            }

            @Override
            public boolean erase(GenericKey key) {
                if (key instanceof ItemKey itemKey)
                    return inventorySummary.erase(itemKey.stack());
                return false;
            }

            @Override
            public InventorySummary asSummary() {
                return inventorySummary;
            }
        };
    }
}
