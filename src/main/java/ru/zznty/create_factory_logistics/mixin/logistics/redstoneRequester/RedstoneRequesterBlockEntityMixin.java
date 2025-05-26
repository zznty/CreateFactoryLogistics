package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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

import java.util.Optional;

@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterBlockEntityMixin extends StockCheckingBlockEntity implements GenericRedstoneRequester {
    @Unique
    public GenericOrder createFactoryLogistics$encodedRequest = GenericOrder.empty();

    @Shadow
    public boolean allowPartialRequests, lastRequestSucceeded;
    @Shadow
    public String encodedTargetAdress;

    public RedstoneRequesterBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Overwrite
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
            if (!allowPartialRequests && level instanceof ServerLevel serverLevel) {
                CatnipServices.NETWORK.sendToClientsAround(serverLevel, worldPosition, 32,
                        new RedstoneRequesterEffectPacket(worldPosition, false));
                return;
            }
        }

        GenericLogisticsManager.broadcastPackageRequest(behaviour.freqId,
                                                        LogisticallyLinkedBehaviour.RequestType.REDSTONE,
                                                        createFactoryLogistics$encodedRequest,
                                                        null,
                                                        encodedTargetAdress);

        if (level instanceof ServerLevel serverLevel)
            CatnipServices.NETWORK.sendToClientsAround(serverLevel, worldPosition, 32, new RedstoneRequesterEffectPacket(worldPosition, anySucceeded));
        lastRequestSucceeded = true;
    }

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/codecs/CatnipCodecUtils;decode(Lcom/mojang/serialization/Codec;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;"
            )
    )
    private Optional<PackageOrderWithCrafts> readRequest(Codec<PackageOrderWithCrafts> codec, HolderLookup.Provider registries, Tag tag, Operation<Optional<PackageOrderWithCrafts>> original) {
        createFactoryLogistics$encodedRequest = GenericOrder.read(registries, (CompoundTag) tag);

        return Optional.empty();
    }

    @WrapOperation(
            method = "write",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/codecs/CatnipCodecUtils;encode(Lcom/mojang/serialization/Codec;Lnet/minecraft/core/HolderLookup$Provider;Ljava/lang/Object;)Ljava/util/Optional;"
            )
    )
    private Optional<Tag> writeRequest(Codec<PackageOrderWithCrafts> codec, HolderLookup.Provider registries, Object t, Operation<Optional<Tag>> original) {
        return Optional.of(createFactoryLogistics$encodedRequest.write(registries));
    }

    @WrapOperation(
            method = "writeSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/codecs/CatnipCodecUtils;encode(Lcom/mojang/serialization/Codec;Lnet/minecraft/core/HolderLookup$Provider;Ljava/lang/Object;)Ljava/util/Optional;"
            )
    )
    private Optional<Tag> writeSafeRequest(Codec<PackageOrderWithCrafts> codec, HolderLookup.Provider registries, Object t, Operation<Optional<Tag>> original) {
        return Optional.of(createFactoryLogistics$encodedRequest.write(registries));
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
