package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.panel.request.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientLogisticsManager;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRedstoneRequester;
import ru.zznty.create_factory_logistics.logistics.stock.IIngredientInventorySummary;

@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterBlockEntityMixin extends StockCheckingBlockEntity implements IngredientRedstoneRequester {
    @Unique
    public IngredientOrder createFactoryLogistics$encodedRequest = IngredientOrder.empty();

    @Shadow(remap = false)
    public boolean allowPartialRequests, lastRequestSucceeded;
    @Shadow(remap = false)
    public String encodedTargetAdress;

    public RedstoneRequesterBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Overwrite(remap = false)
    public void triggerRequest() {
        if (createFactoryLogistics$encodedRequest.isEmpty())
            return;

        boolean anySucceeded = false;

        IIngredientInventorySummary summaryOfOrder = (IIngredientInventorySummary) new InventorySummary();
        createFactoryLogistics$encodedRequest.stacks()
                .forEach(summaryOfOrder::add);

        IIngredientInventorySummary summary = (IIngredientInventorySummary) getAccurateSummary();
        for (BigIngredientStack entry : summaryOfOrder.getStacks()) {
            if (summary.getCountOf(entry) >= entry.getCount()) {
                anySucceeded = true;
                continue;
            }
            if (!allowPartialRequests) {
                AllPackets.sendToNear(level, worldPosition, 32,
                        new RedstoneRequesterEffectPacket(worldPosition, false));
                return;
            }
        }

        IngredientLogisticsManager.broadcastPackageRequest(behaviour.freqId,
                LogisticallyLinkedBehaviour.RequestType.REDSTONE,
                createFactoryLogistics$encodedRequest,
                null,
                encodedTargetAdress);

        AllPackets.sendToNear(level, worldPosition, 32, new RedstoneRequesterEffectPacket(worldPosition, anySucceeded));
        lastRequestSucceeded = true;
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;read(Lnet/minecraft/nbt/CompoundTag;)Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;",
                    ordinal = 0
            ),
            remap = false
    )
    private PackageOrderWithCrafts readRequest(CompoundTag tag, Operation<PackageOrderWithCrafts> original) {
        createFactoryLogistics$encodedRequest = IngredientOrder.read(tag);

        return PackageOrderWithCrafts.empty();
    }

    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;write()Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 0
            ),
            remap = false
    )
    private CompoundTag writeRequest(PackageOrderWithCrafts instance, Operation<CompoundTag> original) {
        return createFactoryLogistics$encodedRequest.write();
    }

    @WrapOperation(
            method = "writeSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;write()Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 0
            ),
            remap = false
    )
    private CompoundTag writeSafeRequest(PackageOrderWithCrafts instance, Operation<CompoundTag> original) {
        return createFactoryLogistics$encodedRequest.write();
    }

    @Override
    public IngredientOrder getOrder() {
        return createFactoryLogistics$encodedRequest;
    }

    @Override
    public void setOrder(IngredientOrder order) {
        createFactoryLogistics$encodedRequest = order;
    }
}
