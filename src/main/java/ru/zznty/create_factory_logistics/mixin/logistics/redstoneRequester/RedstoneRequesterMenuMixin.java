package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericGhostMenu;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;

import java.util.List;

@Mixin(RedstoneRequesterMenu.class)
public abstract class RedstoneRequesterMenuMixin extends GhostItemMenu<RedstoneRequesterBlockEntity> implements GenericGhostMenu {
    protected RedstoneRequesterMenuMixin(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    @WrapOperation(
            method = "createGhostInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;stacks()Ljava/util/List;"
            )
    )
    private List<BigGenericStack> getRequest(PackageOrderWithCrafts instance, Operation<List<BigItemStack>> original) {
        GenericRedstoneRequester requester = (GenericRedstoneRequester) contentHolder;
        return requester.getOrder().stacks().stream().map(BigGenericStack::of).toList();
    }

    @Override
    public GenericStack getGenericSlot(int slot) {
        GenericRedstoneRequester requester = (GenericRedstoneRequester) contentHolder;
        List<GenericStack> stacks = requester.getOrder().stacks();
        return slot < stacks.size() ? stacks.get(slot) : GenericStack.EMPTY;
    }

    @Override
    public void setSlot(int slot, GenericStack stack) {
        GenericRedstoneRequester requester = (GenericRedstoneRequester) contentHolder;
        List<GenericStack> stacks = requester.getOrder().stacks();
        if (slot < stacks.size()) {
            stacks.set(slot, stack);
        } else {
            stacks.add(stack);
        }
    }

    @Override
    public List<GenericStack> getStacks() {
        return ((GenericRedstoneRequester) contentHolder).getOrder().stacks();
    }
}
