package ru.zznty.create_factory_logistics.logistics.ingredient.impl.fluid;

import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

import java.util.Comparator;

@ApiStatus.Internal
public class FluidIngredientProvider implements IngredientKeyProvider {
    private final FluidKeySerializer serializer = new FluidKeySerializer();

    @Override
    public <K extends IngredientKey> K defaultKey() {
        //noinspection unchecked
        return (K) new FluidIngredientKey(Fluids.EMPTY, null);
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
}
