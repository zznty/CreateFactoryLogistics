package ru.zznty.create_factory_logistics.mixin.compat.computercraft;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.compat.computercraft.implementation.peripherals.StockTickerPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.compat.computercraft.GenericDetailsProvider;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("UnusedMixin")
@Mixin(StockTickerPeripheral.class)
public abstract class StockTickerPeripheralMixin extends SyncedPeripheral<StockTickerBlockEntity> {
    public StockTickerPeripheralMixin(StockTickerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    @Overwrite(remap = false)
    public final Map<Integer, Map<String, ?>> stock(Optional<Boolean> detailed) throws LuaException {
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        int i = 0;
        for (GenericStack entry : GenericInventorySummary.of(blockEntity.getAccurateSummary()).get()) {
            result.put(i++, GenericDetailsProvider.detail(entry));
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    @Overwrite(remap = false)
    public final Map<String, ?> getStockItemDetail(int slot) throws LuaException {
        List<GenericStack> stacks = GenericInventorySummary.of(blockEntity.getAccurateSummary()).get();
        int maxSlots = stacks.size();
        if (slot < 1 || slot > maxSlots || Double.isNaN(slot))
            throw new LuaException(
                    String.format("Slot " + slot + " out of range,available slots between " + 1 + " and " + maxSlots));


        GenericStack stack = stacks.get(slot - 1);
        return GenericDetailsProvider.detail(stack);
    }

    @LuaFunction(mainThread = true)
    @Overwrite(remap = false)
    public final int requestFiltered(String address, IArguments filters) throws LuaException {
        List<GenericStack> validStacks = new ArrayList<>();
        int totalSent = 0;
        List<GenericStack> stacks = GenericInventorySummary.of(blockEntity.getAccurateSummary()).get();

        for (int i = 1; i < filters.count(); i++) {
            if (!(filters.get(i) instanceof Map<?, ?> filterTable))
                throw new LuaException("Filter must be a table");

            for (Object key : filterTable.keySet())
                if (!(key instanceof String))
                    throw new LuaException("Filter keys must be strings");

            @SuppressWarnings("unchecked")
            Map<String, Object> filter = (Map<String, Object>) filterTable;

            int requested = Integer.MAX_VALUE;
            if (filterTable.containsKey("_requestCount")) {
                Object requestCount = filterTable.get("_requestCount");
                filterTable.remove("_requestCount");
                if (requestCount instanceof Number) {
                    requested = ((Number) requestCount).intValue();
                    if (requested < 1)
                        throw new LuaException("_requestCount must be a positive number or nil for no limit");
                } else
                    throw new LuaException("_requestCount must be a positive number or nil for no limit");
            }

            for (int j = 0; j < stacks.size(); j++) {
                GenericStack entry = stacks.get(j);
                int foundItems = filterStack(entry, filter);
                if (foundItems > 0) {
                    int toTake = Math.min(foundItems, requested);
                    requested -= toTake;
                    totalSent += toTake;
                    validStacks.add(entry.withAmount(toTake));
                    stacks.set(j, entry.withAmount(entry.amount() - toTake));
                }
                if (requested <= 0)
                    break;
            }
        }

        GenericOrder order = GenericOrder.order(validStacks);

        GenericLogisticsManager.broadcastPackageRequest(blockEntity.behaviour.freqId,
                                                        LogisticallyLinkedBehaviour.RequestType.RESTOCK, order, null,
                                                        address);

        return totalSent;
    }

    @Unique
    private int filterStack(GenericStack stack, Map<?, ?> filter) throws LuaException {
        Map<String, ?> detail = GenericDetailsProvider.detail(stack);
        boolean filterMatches;
        try {
            Method deepEquals = ComputerUtil.class.getDeclaredMethod("deepEquals", Object.class, Object.class);
            deepEquals.setAccessible(true);
            filterMatches = (Boolean) deepEquals.invoke(null, new HashMap<>(filter), detail);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof LuaException luaException)
                throw luaException;
            throw new RuntimeException(e);
        }

        return filterMatches ? stack.amount() : 0;
    }
}
