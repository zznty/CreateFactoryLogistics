# Create Factory Logistics

An addon for the Create 6 mod featuring a unique way of fluid transportation in your logistics network - the **Jar**. It works like packages for items but for liquids, so it can be neatly integrated into your frog-chain logistical system.

## For Modders

This mod contains two projects:

- **FA** - Create Factory Abstractions - a collection of interfaces and classes that can be used to build mods compatible with fluids and other types of content on create logistics network.
- **FL** - Create Factory Logistics - a mod that uses the abstractions to actually shape create logistics network to be extensible which in turn allows it to add fluid support.

For development of compatible, but not direct dependent on FL mods, you would want to compile against FA only and embed it into your mod as JarJar dependency.
For development of mods that depend on FL, you would want to compile against both and depend on FL as a normal mod dependency. Note: in that case you could skip JarJar'ing FA.

### Public Maven

Both projects are available on my public maven repository.

```groovy
repositories {
    // ... other repositories
    maven { url = "https://dl.zznty.ru/maven" } // Create Factory Abstractions, Create Factory Logistics
}
```

### Versioning

Both projects have semantic versioning and have the same version as of right now.

```properties
# Check for updates at https://modrinth.com/mod/create_factory_logistics/versions
create_factory_abstractions_version=1.4.5
```

### Depending on FA

Here is an example of how to depend on FA and embed it into your mod, using architectury loom. You might need to adjust syntax and configuration names depending on your setup.

```groovy
dependencies {
    // ... other dependencies
    modImplementation "ru.zznty:create_factory_abstractions-${minecraft_version}:${create_factory_abstractions_version}"
    include "ru.zznty:create_factory_abstractions-${minecraft_version}:${create_factory_abstractions_version}"
    
    // if you want to test your mod with FL
    modLocalRuntime "ru.zznty:create_factory_logistics-${minecraft_version}:${create_factory_abstractions_version}"
}
```

### Depending on FL

Here is an example of how to depend on both FA and FL, using architectury loom. You might need to adjust syntax and configuration names depending on your setup.

```groovy
dependencies {
    // ... other dependencies
    modImplementation "ru.zznty:create_factory_abstractions-${minecraft_version}:${create_factory_abstractions_version}"
    modImplementation "ru.zznty:create_factory_logistics-${minecraft_version}:${create_factory_abstractions_version}"
}
```
