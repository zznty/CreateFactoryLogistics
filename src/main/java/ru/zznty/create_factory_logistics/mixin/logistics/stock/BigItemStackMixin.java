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
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.impl.item.ItemIngredientKey;

@Mixin(BigItemStack.class)
public class BigItemStackMixin implements BigIngredientStack {

    @Shadow(remap = false)
    public int count;

    @Shadow(remap = false)
    public ItemStack stack;
    @Unique
    private BoardIngredient ingredient;

    @Override
    public BoardIngredient ingredient() {
        return ingredient;
    }

    @Override
    public void setIngredient(BoardIngredient ingredient) {
        this.ingredient = ingredient;
        count = ingredient.amount();
        if (ingredient.key() instanceof ItemIngredientKey itemKey)
            stack = itemKey.stack();
    }

    @Override
    public void setCount(int count) {
        this.count = count;
        ingredient = ingredient.withAmount(count);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean isInfinite() {
        return count >= BigItemStack.INF;
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
        ingredient = stack == ItemStack.EMPTY ? BoardIngredient.of() : new BoardIngredient(IngredientKey.of(stack), count);
    }

    @Overwrite(remap = false)
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        ingredient.write(tag);
        return tag;
    }

    @Overwrite(remap = false)
    public static BigItemStack read(CompoundTag tag) {
        return BigIngredientStack.of(BoardIngredient.read(tag)).asStack();
    }

    @Overwrite(remap = false)
    public void send(FriendlyByteBuf buf) {
        ingredient.write(buf);
    }

    @Overwrite(remap = false)
    public static BigItemStack receive(FriendlyByteBuf buf) {
        return BigIngredientStack.of(BoardIngredient.read(buf)).asStack();
    }

    @Overwrite(remap = false)
    public boolean equals(final Object obj) {
        return obj instanceof BigIngredientStack ingredientStack && ingredient.equals(ingredientStack.ingredient());
    }

    @Overwrite(remap = false)
    public int hashCode() {
        return ingredient.hashCode();
    }
}
