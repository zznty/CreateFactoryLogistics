package ru.zznty.create_factory_logistics;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.JarStyles;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

public class FactoryItems {
    public static final ItemEntry<JarPackageItem> REGULAR_JAR = Builders.jar(JarStyles.REGULAR).register();

    public static final ItemEntry<CompositePackageItem> COMPOSITE_PACKAGE = REGISTRATE
            .item("composite_package", CompositePackageItem::new)
            .defaultModel()
            .properties(p -> p.stacksTo(1))
            .register();

    public static final ItemEntry<Item> FLUID_MECHANISM = REGISTRATE.item("fluid_mechanism", Item::new)
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_FLUID_MECHANISM = REGISTRATE.item("incomplete_fluid_mechanism", SequencedAssemblyItem::new)
            .register();

    // Load this class

    public static void register() {
    }
}
