package ru.zznty.create_factory_logistics.mixin.logistics.panel;

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
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.ItemBoardIngredient;

@Mixin(BigItemStack.class)
public class BigItemStackMixin implements BigIngredientStack {

    @Shadow(remap = false)
    public int count;

    @Shadow(remap = false)
    public ItemStack stack;
    @Unique
    private BoardIngredient ingredient;

    @Override
    public BoardIngredient getIngredient() {
        return ingredient;
    }

    @Override
    public void setIngredient(BoardIngredient ingredient) {
        this.ingredient = ingredient;
        count = ingredient.amount();
        if (ingredient instanceof ItemBoardIngredient itemIngredient)
            stack = itemIngredient.stack();
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
        ingredient = stack == ItemStack.EMPTY ? BoardIngredient.EMPTY : new ItemBoardIngredient(stack.copyWithCount(count));
    }

    @Overwrite(remap = false)
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        ingredient.writeToNBT(tag);
        return tag;
    }

    @Overwrite(remap = false)
    public static BigItemStack read(CompoundTag tag) {
        BoardIngredient ingredient = BoardIngredient.readFromNBT(tag);
        return BigIngredientStack.of(ingredient, ingredient.amount()).asStack();
    }

    @Overwrite(remap = false)
    public void send(FriendlyByteBuf buf) {
        ingredient.write(buf);
    }

    @Overwrite(remap = false)
    public static BigItemStack receive(FriendlyByteBuf buf) {
        BoardIngredient ingredient = BoardIngredient.read(buf);
        return BigIngredientStack.of(ingredient, ingredient.amount()).asStack();
    }

    @Overwrite(remap = false)
    public boolean equals(final Object obj) {
        return obj instanceof BigIngredientStack ingredientStack && ingredient.equals(ingredientStack.getIngredient());
    }

    @Overwrite(remap = false)
    public int hashCode() {
        return ingredient.hashCode();
    }
}
