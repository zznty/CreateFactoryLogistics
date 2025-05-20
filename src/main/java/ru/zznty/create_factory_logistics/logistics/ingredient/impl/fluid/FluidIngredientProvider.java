package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.CapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkFluidHandler;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkMode;

@ApiStatus.Internal
public class FluidIngredientProvider implements IngredientKeyProvider {
    private final FluidKeySerializer serializer = new FluidKeySerializer();

    private final CapabilityFactory<IFluidHandler> capFactory = new CapabilityFactory<>() {
        @Override
        public BlockCapability<IFluidHandler, Direction> capability() {
            return Capabilities.FluidHandler.BLOCK;
        }

        @Override
        public IFluidHandler create(NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour) {
            return new NetworkFluidHandler(behaviour.freqId, mode);
        }
    };

    @Override
    public <K extends IngredientKey> K defaultKey() {
        //noinspection unchecked
        return (K) new FluidIngredientKey(FluidStack.EMPTY.getFluidHolder(), null);
    }

    @Override
    public <T, K extends IngredientKey<T>> K wrap(T value) {
        //noinspection unchecked
        return (K) IngredientKey.of((FluidStack) value);
    }

    @Override
    public <K extends IngredientKey> IngredientKeySerializer<K> serializer() {
        //noinspection unchecked
        return (IngredientKeySerializer<K>) serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K extends IngredientKey> int compare(K a, K b) {
        FluidIngredientKey key1 = (FluidIngredientKey) a;
        FluidIngredientKey key2 = (FluidIngredientKey) b;
        return BuiltInRegistries.FLUID.getKey(key1.fluid().value()).compareTo(BuiltInRegistries.FLUID.getKey(key2.fluid().value()));
    }

    @Override
    public <T> CapabilityFactory<T> capabilityFactory() {
        //noinspection unchecked
        return (CapabilityFactory<T>) capFactory;
    }

    @Override
    public String ingredientTypeUid() {
        return "fluid_stack";
    }
}
