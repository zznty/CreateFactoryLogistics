package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;

@Mixin(BigItemStack.class)
public class BigItemStackMixin implements BigGenericStack {

    @Shadow(remap = false)
    public int count;

    @Shadow(remap = false)
    public ItemStack stack;
    @Unique
    private GenericStack createFactoryLogistics$stack;

    @Unique
    private ItemStack createFactoryLogistics$originalStack;

    @Override
    public GenericStack get() {
        // if count field was written externally, update the stack
        // isn't a good solution but saves most of the overrides for .count field access
        if (count != createFactoryLogistics$stack.amount())
            setAmount(count);
        // consider any external changes to the stack a new state so we synchronize generic to the same value
        if (createFactoryLogistics$originalStack != stack)
            set(GenericStack.wrap(stack).withAmount(count));
        return createFactoryLogistics$stack;
    }

    @Override
    public void set(GenericStack stack) {
        this.createFactoryLogistics$stack = stack;
        count = createFactoryLogistics$stack.amount();
        if (createFactoryLogistics$stack.key() instanceof ItemKey itemKey)
            this.stack = itemKey.stack();
        else if (createFactoryLogistics$stack.isEmpty())
            this.stack = ItemStack.EMPTY;

        // it's an internal write to this.stack so consider it a new initial state
        if (createFactoryLogistics$originalStack != this.stack)
            createFactoryLogistics$originalStack = this.stack;
    }

    @Override
    public void setAmount(int count) {
        this.count = count;
        createFactoryLogistics$stack = createFactoryLogistics$stack.withAmount(count);
    }

    @Override
    public BigItemStack asStack() {
        return (BigItemStack) (Object) this;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("RETURN"),
            remap = false
    )
    private void addIngredient(ItemStack stack, int count, CallbackInfo ci) {
        createFactoryLogistics$stack = GenericStack.wrap(stack).withAmount(count);
        createFactoryLogistics$originalStack = stack;
    }

    @Overwrite(remap = false)
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        GenericStackSerializer.write(createFactoryLogistics$stack, tag);
        return tag;
    }

    @Overwrite(remap = false)
    public static BigItemStack read(CompoundTag tag) {
        return BigGenericStack.of(GenericStackSerializer.read(tag)).asStack();
    }

    @Overwrite(remap = false)
    public void send(FriendlyByteBuf buf) {
        GenericStackSerializer.write(createFactoryLogistics$stack, buf);
    }

    @Overwrite(remap = false)
    public static BigItemStack receive(FriendlyByteBuf buf) {
        return BigGenericStack.of(GenericStackSerializer.read(buf)).asStack();
    }

    @Overwrite(remap = false)
    public boolean equals(final Object obj) {
        return obj instanceof BigGenericStack genericStack && createFactoryLogistics$stack.equals(genericStack.get());
    }

    @Overwrite(remap = false)
    public int hashCode() {
        return createFactoryLogistics$stack.hashCode();
    }
}
