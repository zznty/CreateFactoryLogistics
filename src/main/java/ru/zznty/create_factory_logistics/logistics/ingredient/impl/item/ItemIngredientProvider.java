package ru.zznty.create_factory_logistics.logistics.ingredient.impl.item;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.CapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkItemHandler;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkMode;

@ApiStatus.Internal
public class ItemIngredientProvider implements IngredientKeyProvider {
    private final ItemKeySerializer serializer = new ItemKeySerializer();

    private final CapabilityFactory<IItemHandler> capFactory = new CapabilityFactory<>() {
        @Override
        public BlockCapability<IItemHandler, Direction> capability() {
            return Capabilities.ItemHandler.BLOCK;
        }

        @Override
        public IItemHandler create(NetworkLinkMode mode, LogisticallyLinkedBehaviour behaviour) {
            return new NetworkItemHandler(behaviour.freqId, mode);
        }
    };

    @Override
    public <K extends IngredientKey> K defaultKey() {
        //noinspection unchecked
        return (K) new ItemIngredientKey(Items.AIR.getDefaultInstance());
    }

    @Override
    public <T, K extends IngredientKey<T>> K wrap(T value) {
        //noinspection unchecked
        return (K) IngredientKey.of((ItemStack) value);
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

    @Override
    public String ingredientTypeUid() {
        return "item_stack";
    }
}
