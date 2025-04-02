package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class ItemIngredientProvider implements IngredientKeyProvider {
    private final ItemKeySerializer serializer = new ItemKeySerializer();

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

    @SuppressWarnings("unchecked")
    @Override
    public <K extends IngredientKey> int compare(K a, K b) {
        ItemIngredientKey key1 = (ItemIngredientKey) a;
        ItemIngredientKey key2 = (ItemIngredientKey) b;
        return Integer.compare(key1.stack().getItem().hashCode(), key2.stack().getItem().hashCode());
    }
}
