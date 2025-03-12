package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientGhostMenu;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRedstoneRequester;

import java.util.List;

@Mixin(RedstoneRequesterMenu.class)
public abstract class RedstoneRequesterMenuMixin extends GhostItemMenu<RedstoneRequesterBlockEntity> implements IngredientGhostMenu {
    protected RedstoneRequesterMenuMixin(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    @WrapOperation(
            method = "createGhostInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;stacks()Ljava/util/List;"
            ),
            remap = false
    )
    private List<BigIngredientStack> getRequest(PackageOrder instance, Operation<List<BigItemStack>> original) {
        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) contentHolder;
        return requester.getOrder().stacks();
    }

    @Override
    public BoardIngredient getIngredientInSlot(int slot) {
        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) contentHolder;
        List<BigIngredientStack> stacks = requester.getOrder().stacks();
        return slot < stacks.size() ? stacks.get(slot).getIngredient() : BoardIngredient.EMPTY;
    }

    @Override
    public void setIngredientInSlot(int slot, BoardIngredient ingredient) {
        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) contentHolder;
        List<BigIngredientStack> stacks = requester.getOrder().stacks();
        if (slot < stacks.size()) {
            stacks.set(slot, BigIngredientStack.of(ingredient));
        } else {
            stacks.add(BigIngredientStack.of(ingredient));
        }
    }

    @Override
    public List<BoardIngredient> getIngredients() {
        return ((IngredientRedstoneRequester) contentHolder).getOrder().stacks().stream().map(BigIngredientStack::getIngredient).toList();
    }
}
