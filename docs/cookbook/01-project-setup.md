# Set Up a Project

## Add Factory Abstractions without requiring Factory Logistics

Use this model when your mod should load with Create alone but gain generic logistics behavior when FL is installed. Embed FA because it is a library mod used directly by your classes.

```groovy
repositories {
    maven { url = "https://dl.zznty.ru/maven" }
}

dependencies {
    implementation(jarJar(
        "ru.zznty:create_factory_abstractions-${minecraft_version}:${factory_version}"
    ))

    // Add only to a development run used to test FL interoperability.
    runtimeOnly(
        "ru.zznty:create_factory_logistics-${minecraft_version}:${factory_version}"
    )
}
```

Configuration names vary by Gradle plugin. With Architectury Loom, the equivalent is `modImplementation` plus `include`, as shown in the repository root `README.md`.

This is the model used by Create Mobile Packages 1.21.1. Its release source embeds FA with `jarJar`; FL is an optional integration rather than a declared mandatory mod.

## Depend directly on Factory Logistics

Use this model when your feature has no useful behavior without FL, especially when registering a new generic content kind, packager, or factory panel.

```groovy
repositories {
    maven { url = "https://dl.zznty.ru/maven" }
}

dependencies {
    implementation(
        "ru.zznty:create_factory_abstractions-${minecraft_version}:${factory_version}"
    )
    implementation(
        "ru.zznty:create_factory_logistics-${minecraft_version}:${factory_version}"
    )
}
```

Declare `create_factory_logistics` as mandatory in your mod metadata. Do not also JarJar FA when FL is mandatory: FL already embeds it, and shipping another copy risks duplicate-mod or version conflicts.

## Match the tested platform

Match these four axes before investigating API failures:

- Minecraft and loader version
- Create version
- FA version
- FL version, when installed

FA and FL use the same release version. This checkout compiles against Create `6.0.10-280`, NeoForge `21.1.230`, and Java 21. The FA metadata accepts older Create 6 releases, but compiling and testing against the exact version used by your target pack is safer.

## Decide whether a feature works without FL

Check `CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE` when optional integration code must choose between generic and Create-native behavior.

```java
if (CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE) {
    // Generic summaries and FL mixin-backed adapters are available.
} else {
    // Keep the Create item-only path.
}
```

The flag specifically means that `create_factory_logistics` is loaded. It does not mean that a particular optional content registration, such as Mekanism chemicals, exists.

## Know which APIs are standalone

Without FL:

- `GenericStack.wrap(ItemStack)` works.
- `GenericOrder` converts to and from Create item orders.
- `GenericInventorySummary` wraps Create's item-only `InventorySummary`.
- A consumer mod can embed FA and preserve its ordinary item behavior.

With FL:

- New generic content registrations are activated.
- Create summaries, big stacks, promises, links, and packagers implement FA support interfaces through mixins.
- Non-item stock, package requests, custom package builders, and generic panels participate in the logistics network.

## Test both modes when FL is optional

Run at least these configurations:

1. Create plus your mod, without FL. Exercise every item-only fallback.
2. Create plus FL plus your mod. Exercise non-item stock, request transport, and rendering.
3. A dedicated server with matching client mods. Generic registration IDs are not registry-synced, so both sides must register the same content kinds.

## Version your integration defensively

Prefer interfaces under `ru.zznty.create_factory_abstractions.api`. Support classes under `generic.support`, `logistics.*`, and `compat.*` are used by production integrations but are more coupled to Create internals. `GenericContentExtender` and `GenericStackSerializer` are explicitly marked internal; recipes that need them say so.

Pin the FA/FL version instead of using a dynamic range, and recompile when updating Create. The integration subclasses concrete Create block entities and behaviors, so binary compatibility across Create releases should not be assumed.
