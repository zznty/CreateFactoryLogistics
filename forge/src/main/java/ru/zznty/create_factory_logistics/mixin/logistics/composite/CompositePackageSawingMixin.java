package ru.zznty.create_factory_logistics.mixin.logistics.composite;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.List;

@Mixin(SawBlockEntity.class)
public abstract class CompositePackageSawingMixin extends BlockBreakingKineticBlockEntity {
    @Shadow
    public ProcessingInventory inventory;

    public CompositePackageSawingMixin(BlockEntityType<?> type,
                                       BlockPos pos,
                                       BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "applyRecipe",
            at = @At(value = "RETURN", ordinal = 0)
    )
    private void unpackCompositePackage(CallbackInfo ci, @Local(ordinal = 0) ItemStack input,
                                        @Local List<ItemStack> list) {
        if (input.getItem() instanceof CompositePackageItem) {
            inventory.clear();
            ItemStackHandler results = CompositePackageItem.getContents(getLevel().registryAccess(), input);
            for (int i = 0; i < results.getSlots(); i++) {
                ItemStack stack = results.getStackInSlot(i);
                if (!stack.isEmpty())
                    ItemHelper.addToList(stack, list);
            }
            for (int slot = 0; slot < list.size() && slot + 1 < inventory.getSlots(); slot++)
                inventory.setStackInSlot(slot + 1, list.get(slot));

            for (ItemStack child : CompositePackageItem.getChildren(getLevel().registryAccess(), input)) {
                for (int i = 1; i < inventory.getSlots() - 1; i++) {
                    if (inventory.getStackInSlot(i).isEmpty()) {
                        inventory.setStackInSlot(i, child);
                        break;
                    }
                }
            }
        }
    }
}
