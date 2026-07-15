# Create Factory Abstractions Cookbook

This cookbook is for NeoForge 1.21.1 developers extending Create logistics with content that is not necessarily an item. It is organized by jobs to complete, not by API class.

The examples target the versions used by this repository: Java 21, Minecraft 1.21.1, Create 6.0.10, and matching releases of Create Factory Abstractions (FA) and Create Factory Logistics (FL).

## Choose a recipe

| Goal | Recipe | FL required at runtime? |
| --- | --- | --- |
| Add FA to a mod and choose a dependency model | [Set up a project](01-project-setup.md) | Depends on the integration |
| Add a new logistics content kind | [Register generic content](02-generic-content.md) | Yes |
| Make blocks, items, and packages report generic contents | [Inventories and packages](03-inventories-and-packages.md) | Yes for non-item logistics behavior |
| Package and unpack a new content kind | [Packagers and factory panels](04-packagers-and-panels.md) | Yes |
| Read stock, request packages, render entries, or build a stock screen | [Stock, orders, and UI](05-stock-orders-and-ui.md) | Optional for item-only fallback; yes for generic kinds |
| Diagnose identity, registration, serialization, or capability problems | [Pitfalls and source map](06-pitfalls-and-source-map.md) | N/A |

## Mental model

FA supplies contracts and generic adapters. FL changes Create's logistics implementation with mixins so those contracts operate on Create's summaries, links, packagers, promises, and panels.

There are two common integration styles:

- **Consumer:** Keep stock and requests as `GenericStack`, use `GenericInventorySummary`, send `GenericOrder`, and ask each key's client provider to draw it. This is how [Create Mobile Packages](https://github.com/timplay33/Create-Mobile-Packages) implements its portable stock ticker.
- **Content extension:** Register a new `GenericKey`, serializer, capability adapter, client provider, package builder, packager, and optionally a panel. This is how this repository adds fluids and Mekanism chemicals.

The public API is primarily under `ru.zznty.create_factory_abstractions.api`. Some working recipes currently also require support classes and the internal `GenericContentExtender` registry facade. Those uses are called out explicitly because they are more likely to change between releases.

## Reference implementations

- Fluids: `forge/src/main/java/ru/zznty/create_factory_logistics/logistics/generic`
- Fluid jars and packager: `forge/src/main/java/ru/zznty/create_factory_logistics/logistics/jar` and `logistics/jarPackager`
- Mekanism chemicals, barrels, and panel: `forge/src/main/java/ru/zznty/create_factory_logistics/compat/mekanism`
- Generic API game tests: `forge/src/test/java/ru/zznty/create_factory_logistics/gametest`
- External stock/order/UI consumer: [Create Mobile Packages 1.21.1 release source](https://github.com/timplay33/Create-Mobile-Packages/tree/598e827e05833f79e08174de3b3b723d03d87a12)

## Naming used in examples

The recipes use a fictional `EnergyKey` and `EnergyStack`. Replace these with your native value, capability, package item, and registry. Snippets focus on the FA-specific parts and omit ordinary Registrate/DeferredRegister boilerplate.
