# Inventories and Packages

This chapter makes existing blocks and items visible to generic stock scans, then defines a physical package for a non-item content kind.

## Let FA discover an ordinary native capability

If the content provider from [Register generic content](02-generic-content.md) returns a `GenericCapabilityWrapperProvider`, no extra FA capability is usually needed.

```java
GenericInventory inventory = GenericInventory.of(level, targetPos);
GenericInventorySummary summary = GenericInventorySummary.empty();
GenericInventoryHelper.fillSummary(inventory, summary, level.registryAccess());
```

Resolution for a block is:

1. Return `AbstractionsCapabilities.GENERIC_INVENTORY` immediately if present.
2. Otherwise, query each registered kind's native block capability and adapt it with `unwrap`.

Resolution for an item is:

1. Return `AbstractionsCapabilities.GENERIC_INVENTORY_ITEM` immediately if present.
2. Otherwise, expose Create's package contents as items when present.
3. Otherwise, query each registered kind's native item capability and adapt it with `unwrap`.

Block fallback queries native capabilities with a null side. Register a direct generic inventory if your capability only exists for a specific side or if the item contains several unrelated kinds. A direct generic inventory replaces all fallback for that object; it must answer every content kind the object should expose, or deliberately delegate those registrations itself.

## Expose a custom block directly

Register `GENERIC_INVENTORY` during `RegisterCapabilitiesEvent`:

```java
event.registerBlockEntity(
    AbstractionsCapabilities.GENERIC_INVENTORY,
    ModBlockEntities.ENERGY_VAULT.get(),
    (vault, side) -> registration -> {
        if (registration != energyRegistration()) return null;
        return (summary, registries) -> {
            for (EnergyStack stack : vault.visibleContents())
                summary.add(EnergyGenericExtension.wrapStack(stack));
        };
    }
);
```

`GenericInventory.get(registration)` returns a provider for one requested kind or `null`. It does not return a precomputed mixed list. This lets a heterogeneous inventory answer each registration independently. Because the direct capability shadows native fallback, the example intentionally exposes only energy; add branches for every other kind that the vault should report.

The current public API lacks a registration lookup by key class. Existing integrations use this internal bridge:

```java
private static GenericKeyRegistration energyRegistration() {
    return GenericContentExtender.REGISTRATIONS.get(EnergyKey.class);
}
```

Keep that lookup in one compatibility class so it is easy to replace if a public facade is added.

## Expose a package item directly

Use `GENERIC_INVENTORY_ITEM` when a package's source of truth is a custom data component or when it contains several kinds:

```java
event.registerItem(
    AbstractionsCapabilities.GENERIC_INVENTORY_ITEM,
    (packageStack, ignored) -> registration -> {
        if (registration != energyRegistration()) return null;
        return (summary, registries) -> {
            EnergyStack stored = EnergyPackageItem.getContent(packageStack);
            if (!stored.isEmpty())
                summary.add(EnergyGenericExtension.wrapStack(stored));
        };
    },
    ModItems.ENERGY_CELL_PACKAGE.get()
);
```

If the package already exposes your native item capability, omit the direct FA capability and let FA discover it through normal fallback. A direct registration is useful when native capability exposure would be misleading or incomplete, but then it must report every kind the item should expose.

Do not move Create's `PACKAGE_CONTENTS` fallback into code that runs once per generic registration. It must be checked once before per-kind capability fallback, otherwise the same item contents can be counted multiple times.

## Build a package for your content

Implement `PackageBuilder`. The packager creates a fresh builder, simulates extraction, adds accepted content, and builds the package.

```java
public final class EnergyPackageBuilder implements PackageBuilder {
    private static final int CAPACITY = 10_000;
    private GenericStack content = GenericStack.EMPTY;

    @Override
    public int add(GenericStack incoming) {
        if (!(incoming.key() instanceof EnergyKey))
            throw new IllegalArgumentException("Unsupported content: " + incoming);
        if (!content.isEmpty() && !content.canStack(incoming))
            return -1;

        int stored = content.isEmpty() ? 0 : content.amount();
        int accepted = Math.min(CAPACITY - stored, incoming.amount());
        content = incoming.withAmount(stored + accepted);
        return incoming.amount() - accepted;
    }

    @Override
    public List<GenericStack> content() {
        return content.isEmpty() ? List.of() : List.of(content);
    }

    @Override public boolean isFull() { return content.amount() >= CAPACITY; }
    @Override public int maxPerSlot() { return CAPACITY; }
    @Override public int slotCount() { return 1; }

    @Override
    public PackageMeasureResult measure(GenericKey key) {
        if (!(key instanceof EnergyKey))
            throw new IllegalArgumentException("Unsupported key: " + key);
        return PackageMeasureResult.BULKY;
    }

    @Override
    public ItemStack build() {
        if (content.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = ModItems.ENERGY_CELL_PACKAGE.toStack();
        EnergyPackageItem.setContent(result, content);
        return result;
    }
}
```

Return `EnergyPackageBuilder::new` from your `GenericKeyProviderExtension.packageBuilder()`.

The return value of `add` is important:

- `0`: all input was accepted.
- Positive: this amount remains unpackaged.
- Negative: the key is incompatible with content already accepted by this builder.

`content()` must exactly describe what `build()` will contain. FL subtracts these stacks from network stock after packaging.

`PackageMeasureResult.BULKY` means the content should not share a package with other accepted content. Use `REGULAR` only when your package really supports the slot and stacking behavior reported by `slotCount` and `maxPerSlot`.

## Reuse Create's package item behavior

For a throwable/openable package item, extend `AbstractPackageItem` and use an `AbstractPackageEntity` subclass. Implement at least:

- `getIdSuffix`
- `createEntity`
- `getEntityEntry`
- `openServerSide`

Keep your native item capability or data component as the storage source of truth. The FA generic inventory should be a view over that storage, not a second copy.

Reference implementations:

- `JarPackageItem` and `JarPackageEntity` for fluids
- `BarrelPackageItem` and `BarrelPackageEntity` for Mekanism chemicals

The abstract package base removes custom package items from Create's random cardboard package style lists. Register your own model, renderer, and tooltip as usual.

Override `getDescriptionId()` in a third-party subclass: the current base uses the `create_factory_logistics` translation namespace. Also test every Create transport you support. FL's chain-conveyor entity drop and special rendering paths currently recognize its jar and composite package classes explicitly; merely extending `AbstractPackageItem` does not register a third-party package with those paths. Add your own compatibility mixin/hook where required.

## Verify packaging behavior

Test these cases before connecting a logistics network:

1. Empty builder produces `ItemStack.EMPTY`.
2. A partial add reports the exact leftover.
3. A different key after the first add returns a negative result without mutation.
4. `content()` and the built item's generic inventory report equal stacks.
5. Simulating package extraction never mutates the source.
6. Repackaging preserves package address and `GenericOrder` fragment metadata.

`PackagingGameTests` and `RepackagerGameTests` cover the corresponding fluid/composite paths.
