package ru.zznty.create_factory_logistics.logistics.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable abstraction over stacks of items, fluids, etc.
 * Could be expanded to support other types via registry
 */
public record BoardIngredient(IngredientKey<?> key, int amount) {
    public static final Codec<BoardIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
            IngredientKey.CODEC.fieldOf("key").forGetter(BoardIngredient::key),
            Codec.INT.fieldOf("Amount").forGetter(BoardIngredient::amount)
    ).apply(i, BoardIngredient::new));

    public boolean isEmpty() {
        return amount == 0 || key.provider() == IngredientProviders.EMPTY.get();
    }

    public BoardIngredient withAmount(int amount) {
        return new BoardIngredient(key, amount);
    }

    public boolean canStack(BoardIngredient ingredient) {
        return key.equals(ingredient.key);
    }

    public boolean canStack(IngredientKey<?> otherKey) {
        return key.equals(otherKey);
    }

    public static BoardIngredient read(RegistryFriendlyByteBuf buf) {
        ResourceKey<IngredientKeyProvider> provider = buf.readResourceKey(IngredientRegistry.REGISTRY.key());
        return new BoardIngredient(IngredientRegistry.REGISTRY.get(provider).serializer().read(buf), buf.readVarInt());
    }

    public static BoardIngredient read(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        int amount = tag.getInt("Amount");
        IngredientKeyProvider provider = IngredientRegistry.REGISTRY.get(ResourceLocation.parse(tag.getString("id")));

        return provider == null ?
               BoardIngredient.of() :
               new BoardIngredient(provider.serializer().read(levelRegistryAccess, tag.getCompound("key")), amount);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(IngredientRegistry.REGISTRY.getResourceKey(key.provider()).get());
        key.provider().serializer().write(key, buf);
        buf.writeVarInt(amount);
    }

    public void write(HolderLookup.Provider levelRegistryAccess, CompoundTag tag) {
        tag.putInt("Amount", amount);

        ResourceLocation resourceLocation = IngredientRegistry.REGISTRY.getKey(key.provider());
        if (resourceLocation == null)
            resourceLocation = IngredientProviders.EMPTY.getId();

        tag.putString("id", resourceLocation.toString());

        CompoundTag keyTag = new CompoundTag();
        key.provider().serializer().write(levelRegistryAccess, key, keyTag);
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
