package ru.zznty.create_factory_abstractions.generic.stack;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

@ApiStatus.Internal
public final class GenericStackSerializer {
    public static GenericStack read(RegistryFriendlyByteBuf buf) {
        GenericKeyRegistration provider = GenericContentExtender.REGISTRY.get(
                buf.readResourceKey(GenericContentExtender.REGISTRY.key()));
        if (provider == null) return GenericStack.EMPTY;
        return new GenericStack(provider.serializer().read(buf), buf.readVarInt());
    }

    public static GenericStack read(HolderLookup.Provider registries, CompoundTag tag) {
        int amount = tag.getInt("Amount");
        GenericKeyRegistration provider = GenericContentExtender.REGISTRY.get(
                ResourceLocation.parse(tag.getString("id")));

        return provider == null || !tag.contains("key") ?
               GenericStack.EMPTY :
               new GenericStack(provider.serializer().read(registries, tag.getCompound("key")), amount);
    }

    public static void write(GenericStack value, RegistryFriendlyByteBuf buf) {
        GenericKeyRegistration registration = GenericContentExtender.REGISTRATIONS.get(value.key().getClass());
        buf.writeResourceKey(GenericContentExtender.REGISTRY.getResourceKey(registration).get());
        registration.serializer().write(value.key(), buf);
        buf.writeVarInt(value.amount());
    }

    public static void write(HolderLookup.Provider registries, GenericStack value, CompoundTag tag) {
        tag.putInt("Amount", value.amount());

        if (value.key() == GenericKey.EMPTY) {
            return;
        }

        GenericKeyRegistration registration = GenericContentExtender.REGISTRATIONS.get(value.key().getClass());

        ResourceLocation resourceLocation = GenericContentExtender.REGISTRY.getKeyOrNull(registration);
        if (resourceLocation == null)
            return;

        tag.putString("id", resourceLocation.toString());

        CompoundTag keyTag = new CompoundTag();
        registration.serializer().write(value.key(), registries, keyTag);
        tag.put("key", keyTag);
    }
}
