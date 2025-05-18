package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.CapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkFluidHandler;

import java.util.Comparator;

@ApiStatus.Internal
public class FluidIngredientProvider implements IngredientKeyProvider {
    private final FluidKeySerializer serializer = new FluidKeySerializer();

    private final CapabilityFactory<IFluidHandler> capFactory = (cap, mode, behaviour) ->
            ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> new NetworkFluidHandler(behaviour.freqId, mode)));

    @Override
    public <K extends IngredientKey> K defaultKey() {
        //noinspection unchecked
        return (K) new FluidIngredientKey(Fluids.EMPTY, null);
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
        Comparator order = Comparator.naturalOrder();
        return order.compare(key1.fluid(), key2.fluid());
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
