# Stock, Orders, and UI

This chapter is for consumer mods: portable stock tickers, request terminals, computer integrations, and other features that should work with every registered content kind.

Create Mobile Packages uses this approach in production. It does not register a new generic kind; it consumes summaries, orders, search helpers, and client render providers.

## Convert Create network stock to a generic summary

```java
public static GenericInventorySummary getNetworkStock(UUID network) {
    return GenericInventorySummary.of(
        LogisticsManager.getSummaryOfNetwork(network, true)
    );
}
```

Useful operations are type-agnostic:

```java
GenericInventorySummary summary = GenericInventorySummary.empty();
summary.add(stack);
summary.add(otherSummary);

int count = summary.getCountOf(stack.key());
boolean removed = summary.erase(stack.key());
List<GenericStack> entries = summary.get();
```

When FL is absent, the wrapper retains Create item entries only. When FL is present, its mixins retain all registered kinds.

Do not treat `get()` as an inventory you can mutate. Build a separate list for sorting, filtering, or client state.

## Request generic packages

Build an order and broadcast it on the server:

```java
GenericOrder order = GenericOrder.order(List.of(
    requestedStack.withAmount(requestedAmount)
));

boolean accepted = GenericLogisticsManager.broadcastPackageRequest(
    networkId,
    LogisticallyLinkedBehaviour.RequestType.PLAYER,
    order,
    ignoredInventory,
    address
);
```

A true return means a non-empty request set was submitted and its selected packagers were not too busy. It does not prove that every requested amount was assigned: the current manager can submit a partially covered order. It also does not mean a package has reached its destination.

For all-or-nothing dispatch, do not preflight and then call `broadcastPackageRequest`, because that allocates a second time. Call `findPackagersForRequest` once, sum its `GenericRequest` counts by exact requested key, reject unless every amount is covered, check each selected packager with `isTooBusyFor(type)`, and pass that same multimap to `performPackageRequests`. This is still a server-tick allocation check, not a delivery guarantee.

For item crafting requests, convert Create's order:

```java
GenericOrder order = GenericOrder.of(packageOrderWithCrafts);
```

`GenericOrder.asCrafting()` converts back to Create's representation when an item-only compatibility path needs it.

The order stores lists directly rather than defensively copying them. Pass stable lists or copy mutable UI state before sending it to server logic.

Treat packet input as untrusted. A server handler must derive or validate the network and destination from authoritative state, check player permission and interaction distance, reject non-positive or excessive amounts, and rate-limit stock refreshes and requests. `GenericLogisticsManager` does not perform those player-facing authorization checks for you.

## Put generic stacks in a packet

FA currently has no public `GenericStack.STREAM_CODEC`. The working 1.21.1 pattern, also used by Create Mobile Packages, wraps the internal serializer:

```java
public static final StreamCodec<RegistryFriendlyByteBuf, GenericStack>
        GENERIC_STACK_STREAM_CODEC = StreamCodec.of(
    (buffer, stack) -> GenericStackSerializer.write(stack, buffer),
    GenericStackSerializer::read
);

public static final StreamCodec<RegistryFriendlyByteBuf, GenericOrder>
        GENERIC_ORDER_STREAM_CODEC = StreamCodec.of(
    (buffer, order) -> order.write(buffer),
    GenericOrder::read
);
```

`GenericStackSerializer` is explicitly internal. Keep these codecs in one compatibility class so a future public codec can replace them.

Use `RegistryFriendlyByteBuf`, not a plain `FriendlyByteBuf`. Key serializers can require registry holders.

For large stock lists, chunk packets and only replace client state after the final chunk arrives:

```java
if (allStacks.isEmpty())
    sendToClient(new StockChunk(List.of(), true));

for (int from = 0; from < allStacks.size(); from += MAX_PER_PACKET) {
    int to = Math.min(from + MAX_PER_PACKET, allStacks.size());
    boolean last = to == allStacks.size();
    sendToClient(new StockChunk(List.copyOf(allStacks.subList(from, to)), last));
}
```

Clear a server-side transfer or assign a transfer ID if several updates can overlap. A single global collection buffer, as in a simple screen, assumes only one stock transfer is active.

## Render any registered key

Do not branch on `ItemKey`, `FluidKey`, or third-party classes. Ask the registration to render itself:

```java
GenericStack stack = entry.get();
GenericKeyRegistration registration =
    GenericContentExtender.registrationOf(stack.key());
GenericKeyClientGuiHandler<GenericKey> gui =
    registration.clientProvider().guiHandler();

gui.renderSlot(graphics, stack.key(), x, y);
gui.renderDecorations(graphics, stack.key(), stack.amount(), x, y);
List<Component> tooltip = gui.tooltipBuilder(stack.key(), stack.amount());
```

The generic method signatures can require a local unchecked helper in real code:

```java
@SuppressWarnings({"rawtypes", "unchecked"})
private static GenericKeyClientGuiHandler guiHandler(GenericStack stack) {
    return GenericContentExtender.registrationOf(stack.key())
        .clientProvider().guiHandler();
}
```

`GenericContentExtender` is internal, but there is currently no public key-to-registration lookup. Isolate the dependency.

The GUI handler owns:

- slot contents
- amount decoration and units
- display name with or without amount
- tooltip

This is the key pattern that lets Create Mobile Packages display FL fluids and other extensions without linking their classes.

## Add generic search to a stock screen

Implement `CategoriesProvider`:

```java
public final class RequestScreen implements CategoriesProvider {
    @Override public List<ItemStack> categories() { return categoryIcons; }
    @Override public Set<Integer> hiddenCategories() { return hidden; }
    @Override public List<List<BigGenericStack>> currentItemSource() { return groupedStock; }
}
```

Then delegate search:

```java
GenericSearch.SearchResult result = GenericSearch.search(
    this, searchBox.getValue(), rowHeight, columns
);
displayedCategories = result.categories();
displayedEntries = result.displayedItems();
```

Search modes are:

- ordinary localized name or resource path
- `@namespace`
- `#tag`

Keep `currentItemSource()` aligned with `categories()`: the helper expects one source list per category plus the final unsorted group. Every displayed key needs a client GUI provider. `@` and `#` search additionally require the key provider's `resourceKey()` to return a registered holder.

## Reuse Create's crafting-order interactions

Implement `OrderProvider` on a request screen:

```java
public final class RequestScreen implements OrderProvider {
    @Override public List<BigGenericStack> itemsToOrder() { return selected; }
    @Override public List<CraftableGenericStack> recipesToOrder() { return recipes; }
    @Override public Level world() { return minecraft.level; }
    @Override public GenericInventorySummary stockSnapshot() { return stock; }

    @Override
    public BigGenericStack orderForStack(GenericStack stack) {
        return selected.stream()
            .filter(entry -> entry.get().canStack(stack))
            .findFirst()
            .orElse(null);
    }
}
```

Adjust a recipe request with:

```java
RecipeRequestHelper.requestCraftable(this, recipe, requestedDifference);
```

Revalidate after direct changes:

```java
boolean allOrdersStillUsed = RecipeRequestHelper.updateCraftableAmounts(this);
```

The helper mutates `itemsToOrder()`, recipe amounts, and `BigGenericStack` objects. Return mutable collections owned by the screen.

`GenericIngredient.ofRecipe()` converts vanilla item ingredients. A recipe system with non-item inputs must build its own `GenericIngredient` predicates and `CraftableGenericStack` implementation.

## Add JEI transfer without assuming items

Use FA's `IngredientTransfer` helper to obtain transfer operations, convert each JEI ingredient to a `GenericIngredient`, and populate a generic craftable entry. Check `CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE` before attaching generic recipe data to Create objects that only receive the required support interfaces through FL mixins.

See Create Mobile Packages release `1.21.1-0.7.6` for a complete external implementation:

<https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/compat/jei/DroneControllerTransferHandler.java>

## External examples worth copying

- Summary retrieval: [`StockCheckingItem`](https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/items/portable_stock_ticker/StockCheckingItem.java)
- Packet chunking: [`RequestStockUpdate`](https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/items/portable_stock_ticker/RequestStockUpdate.java)
- Serializer-backed codecs: [`FactoryAbstractions`](https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/compat/FactoryAbstractions.java)
- Generic order packet: [`SendPackage`](https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/items/portable_stock_ticker/SendPackage.java)
- Generic rendering, search, and order providers: [`PortableStockTickerScreen`](https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/items/portable_stock_ticker/PortableStockTickerScreen.java)
