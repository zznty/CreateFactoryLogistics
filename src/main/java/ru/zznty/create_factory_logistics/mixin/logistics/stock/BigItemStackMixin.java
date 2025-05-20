package ru.zznty.create_factory_logistics.mixin.logistics.stock;

import com.simibubi.create.content.logistics.BigItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    @Shadow
    public int count;

    @Shadow
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
            at = @At("RETURN")
    )
    private void addIngredient(ItemStack stack, int count, CallbackInfo ci) {
        ingredient = stack == ItemStack.EMPTY ? BoardIngredient.of() : new BoardIngredient(IngredientKey.of(stack), count);
    }

    @Overwrite
    public static BigItemStack receive(RegistryFriendlyByteBuf buf) {
        return BigIngredientStack.of(BoardIngredient.read(buf)).asStack();
    }

    @Overwrite
    public boolean equals(final Object obj) {
        return obj instanceof BigIngredientStack ingredientStack && ingredient.equals(ingredientStack.ingredient());
    }

    @Overwrite
    public int hashCode() {
        return ingredient.hashCode();
    }
}
