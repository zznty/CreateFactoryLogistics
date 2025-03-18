package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

public interface BoardIngredient {
    BoardIngredient EMPTY = new BoardIngredient() {
        @Override
        public int amount() {
            return 0;
        }

        @Override
        public boolean hasEnough(InventorySummary summary) {
            return false;
        }

        @Override
        public int getCountIn(InventorySummary summary) {
            return 0;
        }

        @Override
        public BoardIngredient withAmount(int amount) {
            return EMPTY;
        }

        @Override
        public void writeToNBT(CompoundTag tag) {
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByte(0);
        }

        @Override
        public boolean canStack(BoardIngredient ingredient) {
            return ingredient == EMPTY;
        }
    };

    int amount();

    boolean hasEnough(InventorySummary summary);

    int getCountIn(InventorySummary summary);

    BoardIngredient withAmount(int amount);

    void writeToNBT(CompoundTag tag);

    void write(FriendlyByteBuf buf);

    boolean canStack(BoardIngredient ingredient);

    static BoardIngredient read(FriendlyByteBuf buf) {
        int mode = buf.readByte();
        if (mode == 0) return EMPTY;
        if (mode == 1) return new ItemBoardIngredient(buf.readItem());
        if (mode == 2) return new FluidBoardIngredient(buf.readFluidStack());

        throw new IllegalStateException("Unknown mode: " + mode);
    }

    static BoardIngredient readFromNBT(CompoundTag tag) {
        if (tag.contains("Item")) {
            ItemStack stack = ItemStack.of(tag.getCompound("Item"));
            int amount = tag.getInt("Amount");
            return new ItemBoardIngredient(stack.copyWithCount(amount));
        }

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag);

        if (fluidStack == FluidStack.EMPTY) return EMPTY;

        return new FluidBoardIngredient(fluidStack);
    }

    static BoardIngredient of(FactoryPanelBehaviour behaviour) {
        int count = behaviour.upTo ? behaviour.recipeOutput : behaviour.count;
        if (count == 0) return EMPTY;
        if (behaviour instanceof FactoryFluidPanelBehaviour fluidBehaviour) {
            if (fluidBehaviour.getFluid() == FluidStack.EMPTY) return EMPTY;

            return new FluidBoardIngredient(fluidBehaviour.getFluid()).withAmount(count);
        }

        if (behaviour.getFilter() == ItemStack.EMPTY) return EMPTY;

        return new ItemBoardIngredient(behaviour.getFilter().copyWithCount(count));
    }
}
