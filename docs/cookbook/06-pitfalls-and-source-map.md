# Pitfalls and Source Map

Use this page when an integration compiles but behaves incorrectly at registration, stock aggregation, packaging, networking, or rendering time.

## Registration never appears

Check all of these:

- FL is loaded. Third-party `GenericContentExtension` instances are ignored otherwise.
- The extension is instantiated from the mod constructor, before the generic-key `RegisterEvent`.
- Only one extension is instantiated for the mod ID. The first instance wins.
- Common registration supplies both provider and serializer.
- Common and client calls use the same path, such as `energy`.
- Optional dependencies are tested identically in common and client registration.

The registry is created with synchronization disabled. Client and server must still register matching IDs because packets write those IDs.

## A key cannot serialize

Likely causes:

- The exact runtime key class was not registered. Registration lookup is not polymorphic.
- The network serializer reads a different sequence than it writes.
- A plain `FriendlyByteBuf` was used instead of `RegistryFriendlyByteBuf`.
- Client and server have different registrations.
- An amount exceeded the `int` model.

NBT reads of unknown registrations become `GenericStack.EMPTY`. Network reads cannot safely skip an unknown key payload and may desynchronize the rest of the packet.

## Equal-looking stock does not merge

`GenericStack.canStack` delegates to key equality. Check:

- Component maps contain only identity data.
- `equals` and `hashCode` agree.
- Canonicalizers were registered before first key comparison.
- `wrap` and serializer round trips preserve the same identity.
- Key equality matches the exact grouping needed by summaries and promises. Their current key-based paths do not invoke the extension's `wrapGeneric(Value)`.

Do not construct another empty key. `GenericStack.isEmpty()` checks `amount == 0 || key == GenericKey.EMPTY`; the key check uses identity. Use `GenericStack.EMPTY` for a truly absent key.

`GenericKey.COMPARATOR` is not guaranteed to be a total ordering. Different registrations compare as zero, and a kind's provider comparator may ignore components or collide. Do not use it for a `TreeMap` or `TreeSet` unless you add an exact-identity tie-breaker.

## Stock is counted more than once

Common causes:

- Application code separately scans a direct generic inventory and its native capability. `GenericInventory.of` itself chooses the direct capability instead of native fallback.
- A summary provider loops physical views that alias the same tank.
- Create package contents are checked once per registration instead of once before capability fallback.
- A package stores one source of truth but exposes stale duplicated metadata.

Prefer one native source of truth and make `GenericInventory` a read-only view over it.

## A capability-backed block is invisible

`GenericInventory.of(level, pos)` asks native capability wrappers with a null side. If your handler requires a side, register `AbstractionsCapabilities.GENERIC_INVENTORY` directly and apply your own side policy.

`GenericCapabilityWrapperProvider.wrap` is used to expose logistics stock as a native capability on a network link. Treat it as read-only. It is not an extraction API for requests.

## A package loses or duplicates content

Check the package builder contract:

- Positive `add` return is the leftover, not accepted amount.
- Negative return means incompatible with existing builder content.
- Simulation and execution extract the same key and amount.
- `content()` reports exactly what the built package stores.
- `build()` returns empty when no content was accepted.
- `slotCount`, `maxPerSlot`, and `isFull` describe actual capacity.
- `unwrap(..., true)` does not mutate the destination.
- `unwrap(..., false)` executes only after full insertion fits.

Every non-item kind that can reach FL's composite repackager should provide a non-null package builder.

## A panel shows the wrong amount

Use one unit throughout:

- native stack amount
- `GenericStack.amount`
- panel `filter().amount()`
- package capacity
- GUI decoration and tooltip
- promise amount

The panel behavior must implement `GenericFilterProvider`. Return `GenericStack.EMPTY` for no filter. Do not blindly copy item panel stack-size multiplication or fluid bucket scaling.

For panel drops, `AbstractFactoryPanelBlockEntity.destroy()` already calls `popPanel(activePanels() - 1)`. Return the requested count without another subtraction.

## A generic screen crashes on a new content kind

Do not cast keys or render stacks yourself. Resolve the registration and delegate to its client GUI handler. Confirm the new kind registered a matching client provider.

`GenericSearch` also dereferences that provider. Mod and tag search depend on `resourceKey()` returning a real registry key.

## API stability boundaries

Prefer these public packages:

```text
ru.zznty.create_factory_abstractions.api.generic
ru.zznty.create_factory_abstractions.api.generic.capability
ru.zznty.create_factory_abstractions.api.generic.crafting
ru.zznty.create_factory_abstractions.api.generic.extensibility
ru.zznty.create_factory_abstractions.api.generic.key
ru.zznty.create_factory_abstractions.api.generic.search
ru.zznty.create_factory_abstractions.api.generic.stack
```

These are useful production support APIs but are coupled more closely to Create and FL implementation:

```text
ru.zznty.create_factory_abstractions.generic.support
ru.zznty.create_factory_abstractions.logistics.box
ru.zznty.create_factory_abstractions.logistics.packager
ru.zznty.create_factory_abstractions.logistics.panel
ru.zznty.create_factory_abstractions.compat.jei
ru.zznty.create_factory_abstractions.compat.computercraft
```

These types are explicitly internal, despite being needed for a few current recipes:

```text
ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender
ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer
```

Wrap internal calls in one compatibility class rather than spreading them through UI, capability, and packet code.

Do not build an integration around `registry.TypeRegistration`, `TypeImplementation`, or `TypeRegistries`; the current type registry lifecycle is empty and has no production consumers.

## In-repository source map

| Task | Primary API | Working implementation |
| --- | --- | --- |
| Register a kind | `GenericContentExtension` | `FactoryGenericExtension` |
| Define component identity | `ConcreteGenericKey` | `FluidKey` |
| Serialize a key | `GenericKeySerializer` | `FluidKeySerializer`, `ChemicalKeySerializer` |
| Adapt a native capability | `GenericCapabilityWrapperProvider` | `FluidGenericExtension`, `ChemicalGenericExtension` |
| Render slots and tooltips | `GenericKeyClientGuiHandler` | `FluidClientGuiHandler` |
| Report package contents | `GenericInventory` | `FactoryCapabilities.JarPackageInventory` |
| Build packages | `PackageBuilder` | `JarPackageBuilder`, `BarrelPackageBuilder` |
| Attach a packager | `PackagerAttachedHandler` | `JarPackagerAttachedHandler` |
| Implement a packager block | `AbstractPackagerBlock*` | `JarPackagerBlock*` |
| Implement a panel | `AbstractFactoryPanel*` | `FactoryChemicalPanel*` |
| Aggregate stock | `GenericInventorySummary` | `InventorySummaryMixin` and summary game tests |
| Send orders | `GenericOrder`, `GenericLogisticsManager` | panel request dispatcher and logistics game tests |
| Search/request UI | `GenericSearch`, `RecipeRequestHelper` | `StockKeeperRequestScreenMixin` |

## External source map

Create Mobile Packages 1.21.1 is a useful example of consuming FA without defining a new key:

- Repository: <https://github.com/timplay33/Create-Mobile-Packages>
- Dependency setup: <https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/build.gradle>
- Generic integration codecs: <https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/compat/FactoryAbstractions.java>
- Network stock and requests: <https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/items/portable_stock_ticker/StockCheckingItem.java>
- Generic stock UI: <https://github.com/timplay33/Create-Mobile-Packages/blob/598e827e05833f79e08174de3b3b723d03d87a12/src/main/java/de/theidler/create_mobile_packages/items/portable_stock_ticker/PortableStockTickerScreen.java>

These source links pin release `1.21.1-0.7.6`. Older integration commits and the 1.20.1 branch contain obsolete direct FL APIs and pre-1.21 networking forms.
