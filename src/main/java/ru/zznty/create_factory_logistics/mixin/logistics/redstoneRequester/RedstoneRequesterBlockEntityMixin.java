package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
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
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterBlockEntityMixin extends StockCheckingBlockEntity implements IngredientRedstoneRequester {
    @Unique
    public IngredientOrder createFactoryLogistics$encodedRequest = IngredientOrder.empty();
    @Unique
    public IngredientOrder createFactoryLogistics$encodedRequestContext = IngredientOrder.empty();

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

        IFluidInventorySummary summaryOfOrder = (IFluidInventorySummary) new InventorySummary();
        createFactoryLogistics$encodedRequest.stacks()
                .forEach(summaryOfOrder::add);

        IFluidInventorySummary summary = (IFluidInventorySummary) getAccurateSummary();
        for (BigIngredientStack entry : summaryOfOrder.get()) {
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
                encodedTargetAdress,
                createFactoryLogistics$encodedRequestContext.isEmpty() ? null : createFactoryLogistics$encodedRequestContext);

        AllPackets.sendToNear(level, worldPosition, 32, new RedstoneRequesterEffectPacket(worldPosition, anySucceeded));
        lastRequestSucceeded = true;
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;read(Lnet/minecraft/nbt/CompoundTag;)Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;",
                    ordinal = 0
            ),
            remap = false
    )
    private PackageOrder readRequest(CompoundTag tag, Operation<PackageOrder> original) {
        createFactoryLogistics$encodedRequest = IngredientOrder.read(tag);

        return PackageOrder.empty();
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;read(Lnet/minecraft/nbt/CompoundTag;)Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;",
                    ordinal = 1
            ),
            remap = false
    )
    private PackageOrder readRequestContext(CompoundTag tag, Operation<PackageOrder> original) {
        createFactoryLogistics$encodedRequestContext = IngredientOrder.read(tag);

        return PackageOrder.empty();
    }

    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;write()Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 0
            ),
            remap = false
    )
    private CompoundTag writeRequest(PackageOrder instance, Operation<CompoundTag> original) {
        return createFactoryLogistics$encodedRequest.write();
    }

    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;write()Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 1
            ),
            remap = false
    )
    private CompoundTag writeRequestContext(PackageOrder instance, Operation<CompoundTag> original) {
        return createFactoryLogistics$encodedRequestContext.write();
    }

    @WrapOperation(
            method = "writeSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;write()Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 0
            ),
            remap = false
    )
    private CompoundTag writeSafeRequest(PackageOrder instance, Operation<CompoundTag> original) {
        return createFactoryLogistics$encodedRequest.write();
    }

    @WrapOperation(
            method = "writeSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;write()Lnet/minecraft/nbt/CompoundTag;",
                    ordinal = 1
            ),
            remap = false
    )
    private CompoundTag writeSafeRequestContext(PackageOrder instance, Operation<CompoundTag> original) {
        return createFactoryLogistics$encodedRequestContext.write();
    }

    @Override
    public IngredientOrder getOrder() {
        return createFactoryLogistics$encodedRequest;
    }

    @Override
    public IngredientOrder getOrderContext() {
        return createFactoryLogistics$encodedRequestContext;
    }

    @Override
    public void setOrder(IngredientOrder order) {
        createFactoryLogistics$encodedRequest = order;
    }

    @Override
    public void setOrderContext(IngredientOrder orderContext) {
        createFactoryLogistics$encodedRequestContext = orderContext;
    }
}
