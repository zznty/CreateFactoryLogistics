package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.CapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkItemHandler;

@ApiStatus.Internal
public class ItemIngredientProvider implements IngredientKeyProvider {
    private final ItemKeySerializer serializer = new ItemKeySerializer();

    private final CapabilityFactory<IItemHandler> capFactory = (cap, mode, behaviour) ->
            ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> new NetworkItemHandler(behaviour.freqId, mode)));

    @Override
    public <K extends IngredientKey> K defaultKey() {
        //noinspection unchecked
        return (K) new ItemIngredientKey(Items.AIR.getDefaultInstance());
    }

    @Override
    public <K extends IngredientKey> IngredientKeySerializer<K> serializer() {
        //noinspection unchecked
        return (IngredientKeySerializer<K>) serializer;
    }

    @Override
    public <K extends IngredientKey> int compare(K a, K b) {
        ItemIngredientKey key1 = (ItemIngredientKey) a;
        ItemIngredientKey key2 = (ItemIngredientKey) b;
        return Integer.compare(key1.stack().getItem().hashCode(), key2.stack().getItem().hashCode());
    }

    @Override
    public <T> CapabilityFactory<T> capabilityFactory() {
        //noinspection unchecked
        return (CapabilityFactory<T>) capFactory;
    }
}
