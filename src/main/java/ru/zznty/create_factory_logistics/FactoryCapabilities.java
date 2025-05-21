package ru.zznty.create_factory_logistics;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.CapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientRegistry;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.jarPackager.JarPackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.packager.BuiltInPackagerAttachedHandler;

public final class FactoryCapabilities {
    public static final BlockCapability<PackagerAttachedHandler, Void> PACKAGER_ATTACHED =
            BlockCapability.create(CreateFactoryLogistics.resource("packager_attached"), PackagerAttachedHandler.class, Void.class);

    public static void register(final RegisterCapabilitiesEvent event) {
        for (IngredientKeyProvider provider : IngredientRegistry.REGISTRY) {
            CapabilityFactory<Object> factory = provider.capabilityFactory();
            if (factory == null) continue;

            event.registerBlockEntity(factory.capability(), FactoryBlockEntities.NETWORK_LINK.get(),
                    (be, side) -> {
                        if (be.provider() == provider && (side == null || PackagerLinkBlock.getConnectedDirection(be.getBlockState())
                                .getOpposite() == side)) {
                            return factory.create(be.mode(), be.getBehaviour(LogisticallyLinkedBehaviour.TYPE));
                        }
                        return null;
                    });
        }

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, unused) ->
                        new FluidHandlerItemStack(FactoryDataComponents.FLUID_CONTENT, stack, Config.jarCapacity),
                FactoryItems.REGULAR_JAR.get());

        event.registerBlockEntity(PACKAGER_ATTACHED, AllBlockEntityTypes.PACKAGER.get(),
                (be, unused) -> new BuiltInPackagerAttachedHandler(be));

        event.registerBlockEntity(PACKAGER_ATTACHED, FactoryBlockEntities.JAR_PACKAGER.get(),
                (be, unused) -> new JarPackagerAttachedHandler(be));

        // i hate neoforge for that
        // should have left capability providers in place
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FactoryBlockEntities.JAR_PACKAGER.get(),
                (be, unused) -> new PackagerItemHandler(be));
    }
}
