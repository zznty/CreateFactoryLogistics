package ru.zznty.create_factory_abstractions.generic.stack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

import java.util.Objects;

@ApiStatus.Internal
public final class GenericStackSerializer {
    public static GenericStack read(FriendlyByteBuf buf) {
        GenericKeyRegistration provider = buf.readRegistryIdSafe(GenericKeyRegistration.class);
        return new GenericStack(provider.serializer().read(buf), buf.readVarInt());
    }

    public static GenericStack read(CompoundTag tag) {
        int amount = tag.getInt("Amount");
        GenericKeyRegistration provider = GenericContentExtender.REGISTRY.get().getValue(
                ResourceLocation.parse(tag.getString("id")));

        return provider == null || !tag.contains("key") ?
               GenericStack.EMPTY :
               new GenericStack(provider.serializer().read(tag.getCompound("key")), amount);
    }

    public static void write(GenericStack value, FriendlyByteBuf buf) {
        GenericKeyRegistration registration = GenericContentExtender.REGISTRATIONS.get(value.key().getClass());
        buf.writeRegistryId(GenericContentExtender.REGISTRY.get(), registration);
        registration.serializer().write(value.key(), buf);
        buf.writeVarInt(value.amount());
    }

    public static void write(GenericStack value, CompoundTag tag) {
        tag.putInt("Amount", value.amount());

        if (value.key() == GenericKey.EMPTY) {
            tag.putString("id",
                          Objects.requireNonNull(GenericContentExtender.REGISTRY.get().getDefaultKey()).toString());
            return;
        }

        GenericKeyRegistration registration = GenericContentExtender.REGISTRATIONS.get(value.key().getClass());

        ResourceLocation resourceLocation = GenericContentExtender.REGISTRY.get().getKey(registration);
        if (resourceLocation == null)
            resourceLocation = Objects.requireNonNull(GenericContentExtender.REGISTRY.get().getDefaultKey());

        tag.putString("id", resourceLocation.toString());

        CompoundTag keyTag = new CompoundTag();
        registration.serializer().write(value.key(), keyTag);
        tag.put("key", keyTag);
    }
}
