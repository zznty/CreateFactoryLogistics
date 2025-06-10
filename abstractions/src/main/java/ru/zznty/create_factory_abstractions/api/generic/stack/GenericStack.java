package ru.zznty.create_factory_abstractions.api.generic.stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

public record GenericStack(GenericKey key, int amount) {
    public static final Codec<GenericStack> CODEC = RecordCodecBuilder.create(i ->
                                                                                      i.group(GenericKey.CODEC.fieldOf(
                                                                                                      "key").forGetter(
                                                                                                      GenericStack::key),
                                                                                              Codec.INT.fieldOf(
                                                                                                      "Amount").forGetter(
                                                                                                      GenericStack::amount))
                                                                                              .apply(i,
                                                                                                     GenericStack::new));

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
        return new GenericStack(new ItemKey(stack), stack.getCount());
    }

    public static GenericStack of(FactoryPanelBehaviour behaviour) {
        GenericFilterProvider filterProvider = (GenericFilterProvider) behaviour;
        return filterProvider.filter();
    }
}
