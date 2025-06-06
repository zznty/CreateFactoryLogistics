package ru.zznty.create_factory_logistics;

import com.tterrag.registrate.util.entry.RegistryEntry;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidGaugeDisplaySource;

import static ru.zznty.create_factory_logistics.CreateFactoryLogistics.REGISTRATE;

public class FactoryDisplaySources {

    public static final RegistryEntry<FactoryFluidGaugeDisplaySource> FLUID_GAUGE_STATUS = REGISTRATE.displaySource("fluid_gauge_status", FactoryFluidGaugeDisplaySource::new).register();

    // Load this class

    public static void register() {
    }
}
