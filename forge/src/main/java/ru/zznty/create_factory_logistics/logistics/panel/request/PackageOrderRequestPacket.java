package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.*;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.FactoryPackets;
import ru.zznty.create_factory_logistics.mixin.accessor.StockTickerBlockEntityAccessor;

public class PackageOrderRequestPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
    private final GenericOrder order;
    private final String address;
    private final boolean encodeRequester;

    public PackageOrderRequestPacket(BlockPos pos, GenericOrder order, String address, boolean encodeRequester) {
        super(pos);
        this.order = order;
        this.address = address;
        this.encodeRequester = encodeRequester;
    }

    @Override
    protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
        if (encodeRequester) {
            if (!order.isEmpty())
                AllSoundEvents.CONFIRM.playOnServer(be.getLevel(), pos);
            player.closeContainer();
            program(player, be);
            return;
        }

        if (!order.isEmpty()) {
            AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(be.getLevel(), pos);
            AllAdvancements.STOCK_TICKER.awardTo(player);
            WiFiEffectPacket.send(player.level(), pos);
        }

        GenericLogisticsManager.broadcastPackageRequest(be.behaviour.freqId,
                                                        LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null,
                                                        address);

        ((StockTickerBlockEntityAccessor) be).setPreviouslyUsedAddress(address);
        be.notifyUpdate();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return FactoryPackets.LOGISTICS_PACKAGE_REQUEST;
    }

    public static void write(RegistryFriendlyByteBuf buf, PackageOrderRequestPacket packet) {
        buf.writeBlockPos(packet.pos);
        packet.order.write(buf);
        buf.writeUtf(packet.address);
        buf.writeBoolean(packet.encodeRequester);
    }

    public static PackageOrderRequestPacket read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        GenericOrder order = GenericOrder.read(buf);
        String address = buf.readUtf();
        boolean encodeRequester = buf.readBoolean();
        return new PackageOrderRequestPacket(pos, order, address, encodeRequester);
    }

    private void program(ServerPlayer player, StockTickerBlockEntity be) {
        ItemStack stack = player.getMainHandItem();
        boolean isRequester = AllBlocks.REDSTONE_REQUESTER.isIn(stack);
        boolean isShopCloth = AllTags.AllItemTags.TABLE_CLOTHS.matches(stack);
        if (!isRequester && !isShopCloth)
            return;

        if (isRequester) {
            CompoundTag beTag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            if (beTag.contains("Freq"))
                return;
            beTag.putUUID("Freq", be.behaviour.freqId);
            beTag.putString("Address", address);
            beTag.put("EncodedRequest", order.write(player.level().registryAccess()));
            BlockEntity.addEntityType(beTag, AllBlockEntityTypes.REDSTONE_REQUESTER.get());
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(beTag));
        } else if (isShopCloth && !stack.has(AllDataComponents.AUTO_REQUEST_DATA)) {
            String targetDim = player.level()
                    .dimension()
                    .location()
                    .toString();
            // TODO find a way to support fluids in cloth shops
            AutoRequestData autoRequestData = new AutoRequestData(order.asCrafting(), address, be.getBlockPos(),
                                                                  targetDim, false);

            autoRequestData.writeToItem(BlockPos.ZERO, stack);
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }
}
