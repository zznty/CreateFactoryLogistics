package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllPackets;
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
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;

@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterBlockEntityMixin extends StockCheckingBlockEntity implements GenericRedstoneRequester {
    @Unique
    public GenericOrder createFactoryLogistics$encodedRequest = GenericOrder.empty();

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

        GenericInventorySummary summaryOfOrder = GenericInventorySummary.empty();
        for (GenericStack stack : createFactoryLogistics$encodedRequest.stacks()) {
            summaryOfOrder.add(stack);
        }

        GenericInventorySummary summary = GenericInventorySummary.of(getAccurateSummary());
        for (GenericStack stack : summaryOfOrder.get()) {
            if (summary.getCountOf(stack.key()) >= stack.amount()) {
                anySucceeded = true;
                continue;
            }
            if (!allowPartialRequests) {
                AllPackets.sendToNear(level, worldPosition, 32,
                                      new RedstoneRequesterEffectPacket(worldPosition, false));
                return;
            }
        }

        GenericLogisticsManager.broadcastPackageRequest(behaviour.freqId,
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
        createFactoryLogistics$encodedRequest = GenericOrder.read(tag);

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
    public GenericOrder getOrder() {
        return createFactoryLogistics$encodedRequest;
    }

    @Override
    public void setOrder(GenericOrder order) {
        createFactoryLogistics$encodedRequest = order;
    }
}
