# Register Generic Content

This recipe adds a content kind such as energy, gas, heat, or another registry-backed resource to FL's logistics model.

**Requires Factory Logistics at runtime.** FA only enqueues third-party `GenericContentExtension` instances when FL is loaded.

## 1. Define identity separately from amount

`GenericKey` identifies what can stack. `GenericStack` pairs that identity with an `int` amount.

For a registry holder with data components, extend `ConcreteGenericKey`:

```java
public final class EnergyKey extends ConcreteGenericKey<EnergyType> {
    public EnergyKey(Holder<EnergyType> holder, PatchedDataComponentMap components) {
        super(holder, components);
    }

    public EnergyStack stack() {
        return new EnergyStack(holder, 1, nbt.asPatch());
    }
}
```

For a value whose identity is only a holder, a record is enough:

```java
public record EnergyKey(Holder<EnergyType> holder) implements GenericKey {
    public EnergyStack stack() {
        return new EnergyStack(holder, 1);
    }
}
```

Use full identity in the key. Named items and component-bearing fluids are intentionally separate keys. If component data contains values that do not define identity, add a canonicalizer as described below.

### Canonicalize component data that should not affect stacking

`ConcreteGenericKey` normally compares the holder and complete component map. `GenericKeyCanonicalizer` lets an integration normalize that component map before `equals` and `hashCode` use it.

Consider a mixed-potion fluid from another mod. Its custom data contains both semantic identity and transient concentration data:

```text
mixed_potion
  bottle_type              identity
  color                    identity
  name                     identity
  composition[]
    effect                 identity
    amplifier              identity
    base_duration_ticks    identity
    duration_ticks         transient
    amount_mb              transient
    capped                 transient
  total_mb                 transient
```

Two stacks with the same effects should stack even if their current concentration produced different `amount_mb` and `duration_ticks`. Register a canonicalizer for only that fluid holder:

```java
public final class MixedPotionFluidIdentity {
    private static final ResourceLocation MIXED_POTION =
        ResourceLocation.fromNamespaceAndPath("potion_mod", "mixed_potion");

    public static void register() {
        BuiltInRegistries.FLUID.getOptional(MIXED_POTION)
            .flatMap(BuiltInRegistries.FLUID::getResourceKey)
            .ifPresent(holderKey -> GenericKeyCanonicalizer.register(
                FluidKey.class,
                holderKey,
                MixedPotionFluidIdentity::canonicalize
            ));
    }

    private static FluidKey canonicalize(FluidKey key) {
        CustomData custom = key.nbt().get(DataComponents.CUSTOM_DATA);
        if (custom == null || custom.isEmpty())
            return key;

        CompoundTag root = custom.copyTag();
        if (!root.contains("mixed_potion", Tag.TAG_COMPOUND))
            return key;

        CompoundTag mixed = root.getCompound("mixed_potion");
        CompoundTag identity = new CompoundTag();
        identity.putString("bottle_type", mixed.getString("bottle_type"));
        identity.putInt("color", mixed.getInt("color"));
        identity.putString("name", mixed.getString("name"));

        ListTag composition = new ListTag();
        ListTag source = mixed.getList("composition", Tag.TAG_COMPOUND);
        for (int i = 0; i < source.size(); i++) {
            CompoundTag entry = source.getCompound(i);
            CompoundTag canonicalEntry = new CompoundTag();
            canonicalEntry.putString("effect", entry.getString("effect"));
            canonicalEntry.putInt("amplifier", entry.getInt("amplifier"));
            canonicalEntry.putLong(
                "base_duration_ticks",
                entry.getLong("base_duration_ticks")
            );
            composition.add(canonicalEntry);
        }
        identity.put("composition", composition);
        root.put("mixed_potion", identity);

        PatchedDataComponentMap components = key.nbt().copy();
        components.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return new FluidKey(key.holder(), components);
    }

    private MixedPotionFluidIdentity() {}
}
```

Call `MixedPotionFluidIdentity.register()` once during common startup, after the target holder is available and before any matching `FluidKey` is compared, hashed, or added to a summary.

The holder-specific overload is appropriate because other fluids should retain exact component identity:

```java
GenericKeyCanonicalizer.register(KeyClass.class, holderKey, canonicalizer);
```

Use the type-wide overload only when the normalization rule is valid for every holder represented by that key class:

```java
GenericKeyCanonicalizer.register(KeyClass.class, canonicalizer);
```

A holder-specific canonicalizer takes precedence over a type-wide canonicalizer. Canonicalizers apply only to `ConcreteGenericKey` subclasses, and `ConcreteGenericKey` caches the result of its first canonicalization. Therefore:

- Register before the first equality or hash operation, not lazily when a stack is discovered.
- Return the original key when the expected data is absent.
- Copy components and tags; do not mutate data held by the original key.
- Preserve every field that changes stack identity.
- Remove only derived, quantity-dependent, timestamp, cache, or other transient fields.
- Normalize ordering too if the producer can emit semantically identical lists in different orders.

Serialization still writes the original key data. Canonicalization changes equality and hashing, not the stored or transmitted stack. Test both equal and unequal cases: equal canonical keys must have equal hash codes, while different effect compositions must remain distinct.

## 2. Implement a key serializer

Implement both registry-aware NBT and network forms:

```java
public final class EnergyKeySerializer implements GenericKeySerializer<EnergyKey> {
    @Override
    public EnergyKey read(HolderLookup.Provider registries, CompoundTag tag) {
        ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
        Holder<EnergyType> holder = registries.lookupOrThrow(ModRegistries.ENERGY_TYPE)
            .get(ResourceKey.create(ModRegistries.ENERGY_TYPE, id))
            .orElseThrow();
        return new EnergyKey(holder);
    }

    @Override
    public void write(EnergyKey key, HolderLookup.Provider registries, CompoundTag tag) {
        tag.putString("id", key.holder().getKey().location().toString());
    }

    @Override
    public EnergyKey read(RegistryFriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        Holder<EnergyType> holder = ModRegistries.ENERGY_TYPES
            .getHolder(id)
            .orElseThrow();
        return new EnergyKey(holder);
    }

    @Override
    public void write(EnergyKey key, RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(key.holder().getKey().location());
    }
}
```

Custom registry lookup APIs differ, but the invariant is exact: write and read the same registry key and then the same component payload, in the same order. Return a defined empty/default key for a missing registry value if worlds may retain data after that value is removed. See `FluidKeySerializer` and `ChemicalKeySerializer` for built-in and custom-registry implementations.

Unknown network registrations cannot be skipped safely because their key payload length is not encoded. Client and server must have matching extension IDs and serializers.

## 3. Adapt your native capability

The provider converts native stacks to keys and native capabilities to read-only stock summaries.

```java
public final class EnergyGenericExtension implements
        GenericKeyProviderExtension<EnergyKey, EnergyStack, EnergyType,
                                    IEnergyHandler, IEnergyItemHandler> {

    private final GenericCapabilityWrapperProvider<IEnergyHandler, IEnergyItemHandler>
            capabilities = new GenericCapabilityWrapperProvider<>() {
        @Override
        public BlockCapability<IEnergyHandler, Direction> capability() {
            return ModCapabilities.ENERGY_BLOCK;
        }

        @Override
        public ItemCapability<IEnergyItemHandler, Void> capabilityItem() {
            return ModCapabilities.ENERGY_ITEM;
        }

        @Override
        public GenericInventorySummaryProvider unwrap(IEnergyHandler handler) {
            return (summary, registries) -> {
                for (int slot = 0; slot < handler.tanks(); slot++) {
                    EnergyStack stack = handler.get(slot);
                    if (!stack.isEmpty())
                        summary.add(wrapStack(stack));
                }
            };
        }

        @Override
        public IEnergyHandler wrap(GenericInventorySummaryProvider source,
                                   HolderLookup.Provider registries) {
            return new ReadOnlyNetworkEnergyHandler(source, registries);
        }
    };

    public static GenericStack wrapStack(EnergyStack stack) {
        if (stack.isEmpty()) return GenericStack.EMPTY;
        return new GenericStack(new EnergyKey(stack.holder()), stack.amount());
    }

    @Override public EnergyKey defaultKey() { return new EnergyKey(emptyHolder()); }
    @Override public EnergyKey wrap(EnergyStack stack) { return new EnergyKey(stack.holder()); }
    @Override public EnergyKey wrapGeneric(EnergyStack stack) { return wrap(stack); }
    @Override public EnergyStack unwrap(EnergyKey key) { return key.stack(); }
    @Override public String ingredientTypeUid(EnergyKey key) { return "energy_stack"; }
    @Override public boolean supportsIngredientTypeUid(String uid) {
        return uid.equals("energy_stack");
    }
    @Override public Optional<ResourceKey<EnergyType>> resourceKey(EnergyKey key) {
        return Optional.ofNullable(key.holder().getKey());
    }
    @Override public GenericCapabilityWrapperProvider<IEnergyHandler, IEnergyItemHandler>
            capabilityWrapperProvider() { return capabilities; }
    @Override public Supplier<PackageBuilder> packageBuilder() {
        return EnergyPackageBuilder::new;
    }
    @Override public int stackSize(EnergyKey key) { return 1000; }
    @Override public int maxStackSize(EnergyKey key) { return -1; }
    @Override public int compare(EnergyKey a, EnergyKey b) {
        ResourceKey<EnergyType> aKey = a.holder().getKey();
        ResourceKey<EnergyType> bKey = b.holder().getKey();
        if (aKey == null) return bKey == null ? 0 : -1;
        if (bKey == null) return 1;
        return aKey.compareTo(bKey);
    }
}
```

`wrap` preserves exact identity. `wrapGeneric` is used when the provider receives a native value and needs a genericized key. In the current implementation, summary and promise paths pass an already-created `GenericKey`; the adapter returns that key unchanged and does not call your extension's `wrapGeneric(Value)`. Design key equality as the exact identity required by those paths. Do not rely on `wrapGeneric` to merge component variants there.

Use `-1` from `maxStackSize` for an unlimited logical maximum. All logistics amounts are still `int`; clamp native `long` amounts explicitly.

The object returned by `capabilityWrapperProvider().wrap(...)` should be a snapshot or read-only view of network stock. Do not pretend that extracting from the wrapper mutates the real logistics network.

## 4. Register common and client parts under one ID

Create one extension object for your mod and instantiate it from the mod constructor:

```java
public final class MyContentExtension extends GenericContentExtension {
    public MyContentExtension() {
        super(MyMod.ID);
    }

    @Override
    public void registerCommon(CommonContentRegistration registration) {
        registration.register(MyGenericContent.ENERGY.getPath(), EnergyKey.class, builder -> builder
            .provider(EnergyGenericExtension::new)
            .serializer(EnergyKeySerializer::new));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient(ClientContentRegistration registration) {
        registration.<EnergyKey>register(MyGenericContent.ENERGY.getPath(), builder -> builder
            .clientProvider(EnergyClientProvider::new));
    }
}
```

```java
@Mod(MyMod.ID)
public final class MyMod {
    public MyMod(IEventBus modBus) {
        new MyContentExtension();
        // Register ordinary mod content after enqueueing the extension.
    }
}
```

The registration ID becomes `my_mod:energy`. The common and client strings must match. Only the first `GenericContentExtension` for a mod ID is retained, so register all of your content kinds from one object.

## 5. Supply client rendering

Visible keys need both GUI and world rendering handlers:

```java
public final class EnergyClientProvider
        implements GenericKeyClientProvider<EnergyKey> {
    private final GenericKeyClientGuiHandler<EnergyKey> gui = new EnergyGuiHandler();
    private final GenericKeyClientRenderHandler<EnergyKey> render = new EnergyRenderHandler();

    @Override public GenericKeyClientGuiHandler<EnergyKey> guiHandler() { return gui; }
    @Override public GenericKeyClientRenderHandler<EnergyKey> renderHandler() { return render; }
}
```

```java
public final class EnergyGuiHandler implements GenericKeyClientGuiHandler<EnergyKey> {
    @Override
    public void renderSlot(GuiGraphics graphics, EnergyKey key, int x, int y) {
        EnergySlotRenderer.render(graphics, key.stack(), x, y);
    }

    @Override
    public void renderDecorations(GuiGraphics graphics, EnergyKey key,
                                  int amount, int x, int y) {
        SlotAmountRenderer.render(graphics, x, y, formatAmount(amount));
    }

    @Override public LangBuilder nameBuilder(EnergyKey key) {
        return CreateLang.builder().add(key.stack().getHoverName());
    }
    @Override public LangBuilder nameBuilder(EnergyKey key, int amount) {
        return nameBuilder(key).space().text(formatAmount(amount));
    }
    @Override public List<Component> tooltipBuilder(EnergyKey key, int amount) {
        return List.of(key.stack().getHoverName(), Component.literal(formatAmount(amount)));
    }
}
```

`renderPanelFilter` draws the key attached to a factory panel:

```java
public final class EnergyRenderHandler
        implements GenericKeyClientRenderHandler<EnergyKey> {
    @Override
    public void renderPanelFilter(EnergyKey key, PoseStack pose,
                                  MultiBufferSource buffers, int light, int overlay) {
        EnergyWorldRenderer.render(key.stack(), pose, buffers, light, overlay);
    }
}
```

FL and generic consumer screens assume that a visible registration has a client provider. Do not omit it for a common registration that can reach a GUI.

## 6. Let players qualify a network link

FL's network link stores one generic registration ID and exposes only that kind's native network capability. Generate its qualification recipe and qualifier item tag rather than maintaining the generated data by hand.

Define the generic registration ID once so registration and data generation cannot drift:

```java
public final class MyGenericContent {
    public static final ResourceLocation ENERGY =
        ResourceLocation.fromNamespaceAndPath(MyMod.ID, "energy");

    private MyGenericContent() {}
}
```

Use FL's recipe builder from a standard recipe provider:

```java
public final class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        new NetworkLinkQualificationRecipeBuilder()
            .save(output, MyGenericContent.ENERGY);
    }
}
```

Generate the corresponding qualifier tag with `TagsProvider<Item>`. Items in this tag are accepted next to an unqualified network link in the crafting grid:

```java
public final class ModItemTagsProvider extends TagsProvider<Item> {
    public ModItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFiles) {
        super(output, Registries.ITEM, registries, MyMod.ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(NetworkLinkQualificationRecipe.tag(MyGenericContent.ENERGY))
            .add(itemKey(ModItems.ENERGY_CELL.get()));
    }

    private static ResourceKey<Item> itemKey(ItemLike item) {
        return BuiltInRegistries.ITEM.getResourceKey(item.asItem())
            .orElseThrow();
    }
}
```

`NetworkLinkQualificationRecipe.tag(...)` creates a tag in the `create_factory_logistics` namespace whose path includes your registration namespace and path. Using the helper avoids duplicating that path convention.

Register both providers from your mod's data-gathering listener:

```java
public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();

    generator.addProvider(
        event.includeServer(),
        new ModRecipeProvider(output, event.getLookupProvider())
    );
    generator.addProvider(
        event.includeServer(),
        new ModItemTagsProvider(
            output,
            event.getLookupProvider(),
            event.getExistingFileHelper()
        )
    );
}
```

```java
public MyMod(IEventBus modBus) {
    modBus.addListener(ModDataGen::gatherData);
}
```

Run your normal data-generation task and commit the generated recipe and qualifier tag. Regenerate them when the content ID or qualifier items change; do not edit the generated files directly.

Add `my_mod.gui.ingredient_type.energy` through your language provider for the qualified link tooltip. The item resolves that part from the registration namespace.

This step is needed when players should craft an FL network link qualified for your content kind. It is separate from registering the generic key itself.

## 7. Add focused tests

Before adding a packager or panel, test the content kind itself:

1. NBT and network round trips preserve exact keys and amount.
2. Equal keys merge in `GenericInventorySummary`.
3. Component variants that should differ remain separate.
4. Canonicalized variants that should match have equal hashes.
5. Capability unwrapping reports every native slot/tank once.
6. Native amounts above `Integer.MAX_VALUE` are clamped rather than wrapped.

See `GenericSerializationGameTests`, `GenericInventorySummaryGameTests`, and `FluidIdentityGameTests` in the forge test source.
