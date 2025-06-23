package ru.zznty.create_factory_abstractions.api.generic.stack;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

public record GenericStack(GenericKey key, int amount) {

    public static final GenericStack EMPTY = new GenericStack(GenericKey.EMPTY, 0);

    public boolean isEmpty() {
        return amount == 0 || key == GenericKey.EMPTY;
    }

    public GenericStack withAmount(int amount) {
        return new GenericStack(key, amount);
    }

    public boolean canStack(GenericStack ingredient) {
        return key.equals(ingredient.key);
    }

    public boolean canStack(GenericKey otherKey) {
        return key.equals(otherKey);
    }

    public static GenericStack wrap(ItemStack stack) {
        if (stack.isEmpty()) return EMPTY;
        return new GenericStack(new ItemKey(stack.copyWithCount(1)), stack.getCount());
    }

    public static GenericStack of(FactoryPanelBehaviour behaviour) {
        GenericFilterProvider filterProvider = (GenericFilterProvider) behaviour;
        return filterProvider.filter();
    }

    public static void writeToStream(RegistryFriendlyByteBuf buffer, GenericStack stack) {
        ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer.write(stack, buffer);
    }

    public static GenericStack readFromStream(RegistryFriendlyByteBuf buffer) {
        return ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer.read(buffer);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericStack> STREAM_CODEC = StreamCodec.of(
            GenericStack::writeToStream,
            GenericStack::readFromStream
    );
}
