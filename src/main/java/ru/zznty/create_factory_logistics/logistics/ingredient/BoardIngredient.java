package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Immutable abstraction over stacks of items, fluids, etc.
 * Could be expanded to support other types via registry
 */
public record BoardIngredient(IngredientKey key, int amount) {
    public boolean isEmpty() {
        return amount == 0 || key.provider() == IngredientProviders.EMPTY.get();
    }

    public BoardIngredient withAmount(int amount) {
        return new BoardIngredient(key, amount);
    }

    public boolean canStack(BoardIngredient ingredient) {
        return key.equals(ingredient.key);
    }

    public boolean canStack(IngredientKey otherKey) {
        return key.equals(otherKey);
    }

    public static BoardIngredient read(FriendlyByteBuf buf) {
        IngredientKeyProvider provider = buf.readRegistryIdSafe(IngredientKeyProvider.class);
        return new BoardIngredient(provider.serializer().read(buf), buf.readVarInt());
    }

    public static BoardIngredient read(CompoundTag tag) {
        int amount = tag.getInt("Amount");
        IngredientKeyProvider provider = IngredientRegistry.REGISTRY.get().getValue(ResourceLocation.parse(tag.getString("id")));

        return provider == null ? BoardIngredient.of() : new BoardIngredient(provider.serializer().read(tag.getCompound("key")), amount);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeRegistryId(IngredientRegistry.REGISTRY.get(), key.provider());
        key.provider().serializer().write(key, buf);
        buf.writeVarInt(amount);
    }

    public void write(CompoundTag tag) {
        tag.putInt("Amount", amount);

        ResourceLocation resourceLocation = IngredientRegistry.REGISTRY.get().getKey(key.provider());
        if (resourceLocation == null)
            resourceLocation = Objects.requireNonNull(IngredientRegistry.REGISTRY.get().getDefaultKey());

        tag.putString("id", resourceLocation.toString());

        CompoundTag keyTag = new CompoundTag();
        key.provider().serializer().write(key, keyTag);
        tag.put("key", keyTag);
    }

    public static BoardIngredient of() {
        return new BoardIngredient(IngredientKey.EMPTY, 0);
    }

    public static BoardIngredient of(FactoryPanelBehaviour behaviour) {
        IngredientFilterProvider filterProvider = (IngredientFilterProvider) behaviour;
        return filterProvider.ingredient();
    }
}
