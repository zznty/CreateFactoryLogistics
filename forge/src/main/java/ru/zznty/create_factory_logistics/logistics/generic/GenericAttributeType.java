package ru.zznty.create_factory_logistics.logistics.generic;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;

public interface GenericAttributeType extends ItemAttributeType {
    @NotNull GenericAttribute create();

    List<ItemAttribute> getAllAttributes(GenericStack stack, Level level);

    default @NotNull ItemAttribute createAttribute() {
        return create();
    }

    default List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
        return getAllAttributes(GenericStack.wrap(stack), level);
    }

    default MapCodec<? extends GenericAttribute> codec() {
        return new Codec<GenericAttribute>() {
            @Override
            public <T> DataResult<T> encode(GenericAttribute input, DynamicOps<T> ops, T prefix) {
                CompoundTag tag = new CompoundTag();
                HolderLookup.Provider registries = ((RegistryOps.HolderLookupAdapter) ((RegistryOps<T>) ops).lookupProvider).lookupProvider;
                input.save(registries, tag);
                return CompoundTag.CODEC.encode(tag, ops, prefix);
            }

            @Override
            public <T> DataResult<Pair<GenericAttribute, T>> decode(DynamicOps<T> ops, T input) {
                CompoundTag tag = CompoundTag.CODEC.decode(ops, input).getOrThrow().getFirst();
                GenericAttribute attribute = create();
                HolderLookup.Provider registries = ((RegistryOps.HolderLookupAdapter) ((RegistryOps<T>) ops).lookupProvider).lookupProvider;
                attribute.load(registries, tag);
                return DataResult.success(Pair.of(attribute, input));
            }

            @Override
            public String toString() {
                return "GenericAttributeType";
            }
        }.fieldOf("data");
    }

    default StreamCodec<? super RegistryFriendlyByteBuf, ? extends GenericAttribute> streamCodec() {
        return StreamCodec.ofMember(
                (attribute, buf) -> {
                    CompoundTag nbt = new CompoundTag();
                    attribute.save(buf.registryAccess(), nbt);
                    buf.writeNbt(nbt);
                },
                buf -> {
                    CompoundTag nbt = buf.readNbt();
                    GenericAttribute attribute = create();
                    attribute.load(buf.registryAccess(), nbt);
                    return attribute;
                }
        );
    }
}
