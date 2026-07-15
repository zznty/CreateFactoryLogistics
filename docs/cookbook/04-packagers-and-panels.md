# Packagers and Factory Panels

This chapter connects a native inventory to Create's package request flow and optionally adds a gauge for that content kind.

**Requires Factory Logistics at runtime.** These integrations rely on FL mixins applied to Create packagers, links, summaries, and panels.

## Implement the attached packager handler

`PackagerAttachedHandler` is the adapter between a Create `PackagerBlockEntity` and your native inventory.

```java
public final class EnergyPackagerHandler implements PackagerAttachedHandler {
    private final EnergyPackagerBlockEntity packager;

    public EnergyPackagerHandler(EnergyPackagerBlockEntity packager) {
        this.packager = packager;
    }

    @Override
    public int slotCount() {
        return packager.target().map(IEnergyHandler::tanks).orElse(0);
    }

    @Override
    public GenericStack extract(int slot, int amount, boolean simulate) {
        EnergyStack extracted = packager.target()
            .map(handler -> handler.extract(slot, amount, simulate))
            .orElse(EnergyStack.EMPTY);
        return EnergyGenericExtension.wrapStack(extracted);
    }

    @Override
    public boolean unwrap(Level level, BlockPos pos, BlockState state,
                          Direction side, PackageOrderWithCrafts context,
                          ItemStack box, boolean simulate) {
        if (!EnergyPackageItem.isPackage(box)) return false;
        IEnergyHandler destination = level.getCapability(
            ModCapabilities.ENERGY_BLOCK, pos, state, null, side);
        if (destination == null) return false;

        EnergyStack source = EnergyPackageItem.getContent(box);
        if (!destination.acceptsAll(source, true)) return false;
        return simulate || destination.acceptsAll(source, false);
    }

    @Override public PackageBuilder newPackage() { return new EnergyPackageBuilder(); }
    @Override public GenericKeyRegistration supportedKey() { return energyRegistration(); }
    @Override public Block supportedGauge() { return ModBlocks.ENERGY_GAUGE.get(); }
    @Override
    public IdentifiedInventory identifiedInventory() {
        IEnergyHandler handler = packager.target().orElse(null);
        if (handler == null) return null;

        IdentifiedInventory identified = new IdentifiedInventory(
            InventoryIdentifier.get(packager.targetLevel(), packager.targetFace()),
            null
        );
        GenericIdentifiedInventory.from(identified)
            .setCapability(ModCapabilities.ENERGY_BLOCK, handler);
        return identified;
    }
}
```

The exact capability lookup overload depends on your NeoForge capability and context. The behavioral rules do not:

- `extract(..., true)` and `extract(..., false)` must select the same content.
- `unwrap(..., true)` must test full insertion without mutation.
- `unwrap(..., false)` must execute only after full insertion is known to fit.
- `newPackage()` must return a fresh builder.
- `supportedKey()` must return the exact registration for the extracted kind.
- `identifiedInventory()` should identify the target so a request does not source from and deliver back into the same inventory.

For non-item storage, Create's `IdentifiedInventory` must be augmented through `GenericIdentifiedInventory.setCapability`. Construct its `InventoryIdentifier` from the exact target face, attach the same native handler returned by the packager's manipulation behavior, and compare both handler identity and identifier in the block entity.

Existing integrations currently use `GenericContentExtender.REGISTRATIONS.get(EnergyKey.class)` for `supportedKey()`. Isolate this explicitly internal lookup.

## Register the handler capability

```java
event.registerBlockEntity(
    AbstractionsCapabilities.PACKAGER_ATTACHED,
    ModBlockEntities.ENERGY_PACKAGER.get(),
    (packager, ignored) -> packager.handler
);
```

`PackagerAttachedHandler.get(blockEntity)` reads this capability when FL is loaded and falls back to Create's normal item packager handler for `PackagerBlockEntity`.

## Reuse the packager base classes

`AbstractPackagerBlockEntity` creates the handler and adapts Create's same-inventory check:

```java
public final class EnergyPackagerBlockEntity extends AbstractPackagerBlockEntity {
    public EnergyPackagerBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                     BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected PackagerAttachedHandler createHandler() {
        return new EnergyPackagerHandler(this);
    }

    @Override
    protected boolean targetsSameInventory(GenericIdentifiedInventory inventory) {
        IEnergyHandler target = targetHandler();
        if (target == null) return false;
        if (inventory.handler() == target) return true;
        return inventory.identifier() != null
            && inventory.identifier().contains(targetFace());
    }
}
```

The base constructor calls `createHandler()` before the subclass constructor body runs. The handler may retain the block entity, but it must not eagerly read subclass fields initialized later.

`AbstractPackagerBlock` adds target-facing placement and passes gauge interaction through to Create:

```java
public final class EnergyPackagerBlock extends AbstractPackagerBlock {
    @Override
    protected boolean prefersBlockEntity(BlockEntity target) {
        return target.getLevel().getCapability(
            ModCapabilities.ENERGY_BLOCK, target.getBlockPos(), null) != null;
    }

    @Override protected Class<? extends AbstractPackagerBlockEntity> getBeClass() {
        return EnergyPackagerBlockEntity.class;
    }

    @Override public BlockEntityType<? extends AbstractPackagerBlockEntity>
            getBlockEntityType() {
        return ModBlockEntities.ENERGY_PACKAGER.get();
    }
}
```

Portable Storage Interfaces are rejected by this base unless the `packagerspsic` integration mod is loaded.

## Add a factory panel for the content

A custom gauge consists of a block, block entity, behavior, baked model wrapper, and usually a renderer/screen. The chemical panel is the clearest complete reference.

### Block

```java
public final class EnergyPanelBlock extends AbstractFactoryPanelBlock {
    @Override public BlockEntityType<? extends FactoryPanelBlockEntity>
            getBlockEntityType() {
        return ModBlockEntities.ENERGY_PANEL.get();
    }

    @Override protected Class<? extends AbstractFactoryPanelBlockEntity> getBeClass() {
        return EnergyPanelBlockEntity.class;
    }

    @Override protected BlockEntry<? extends AbstractFactoryPanelBlock> getBlockEntry() {
        return ModBlocks.ENERGY_GAUGE;
    }
}
```

The base handles tuned placement, up to four panel slots, wrench removal, and partial block destruction.

### Block entity

```java
public final class EnergyPanelBlockEntity extends AbstractFactoryPanelBlockEntity {
    @Override
    protected AbstractFactoryPanelBehaviour createBehaviour(PanelSlot slot) {
        return new EnergyPanelBehaviour(this, slot);
    }

    @Override
    protected ItemStack popPanel(int count) {
        return ModBlocks.ENERGY_GAUGE.asStack(count);
    }
}
```

Return exactly `count` items from `popPanel`. `AbstractFactoryPanelBlockEntity.destroy()` already passes `activePanels() - 1`; do not subtract one again.

### Behavior

The essential hook is `GenericFilterProvider.filter()`:

```java
public final class EnergyPanelBehaviour extends AbstractFactoryPanelBehaviour
        implements GenericFilterProvider {
    @Override
    public GenericStack filter() {
        EnergyKey key = selectedKey();
        if (key == null) return GenericStack.EMPTY;
        return new GenericStack(key, count);
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        if (stack.isEmpty()) return super.setFilter(stack);
        if (EnergyContainerItem.read(stack).isEmpty()) return false;
        return super.setFilter(stack);
    }
}
```

The returned stack controls stock lookup, promised amounts, request planning, recipe identity, connection labels, and filter rendering. Preserve the selected key when `count` is zero: zero-target intermediate/cascade panels still need their identity. Return `GenericStack.EMPTY` only when no key is selected. Decide what the panel's `count` means in native units and use the same units in your provider, package builder, and GUI formatter.

Implement the remaining normal `FactoryPanelBehaviour` hooks for your filter menu, value settings, labels, and client screen. `FactoryChemicalPanelBehaviour` demonstrates validating a container item, converting value settings to native units, and supplying a generic chemical filter.

Complete the registration as a specialized Create panel, not a plain block:

1. Register the block with `FactoryPanelBlockItem`, the same placement/blockstate setup as Create's gauge, and a custom item model.
2. Register the block entity for that block and its `SmartBlockEntityRenderer`.
3. Register the menu and client screen used by `createMenu` and `displayScreen`.
4. Persist and synchronize any behavior fields not already handled by `FactoryPanelBehaviour`.
5. Register partial models for passive, bulb, restocker, and restocker-with-bulb states.
6. Wrap the baked block model with your `AbstractPanelModel` subclass.
7. Supply block/item models, textures, language entries, loot, recipe, and render layers.

`FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE`, `FactoryMekanismBlockEntities.FACTORY_CHEMICAL_PANEL`, and the classes in `compat/mekanism/logistics/panel` show the complete wiring.

### Model and renderer

Wrap the panel model with `AbstractPanelModel` or follow `FactoryChemicalPanelModel`, register the block model wrapper, and render the filter through the key's `GenericKeyClientRenderHandler`. This keeps the panel behavior independent from the concrete resource renderer.

## Make the panel act as a restocker

The panel block entity looks behind itself for `PackagerAttachedHandler`. It becomes a restocker only when:

```java
handler.supportedGauge() == panelBlock
```

Return your exact panel block from the handler. The current base still returns a concrete Create `PackagerBlockEntity` from `getRestockedPackager`, so derive your custom packager block entity from Create's packager base as shown above.

## Test the complete request loop

Use a game test with a source inventory, packager, stock link, network, panel, and destination:

1. Simulated extraction leaves source stock unchanged.
2. A request creates packages whose total content equals the accepted request.
3. Partial source stock produces no duplication or negative amount.
4. Destination simulation rejects packages that do not fit.
5. Successful unwrap inserts once and consumes the package through normal Create flow.
6. A panel reads stock and promises in native units.
7. A restocker does not source from its own destination inventory.

Use `PackagingGameTests`, `CreateLogisticsNetworkGameTests`, and the panel game tests as fixture examples.
