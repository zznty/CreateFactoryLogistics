package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ru.zznty.create_factory_logistics.FactoryPackets;

public class PackageOrderRequestPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
    private final IngredientOrder order;
    private final String address;
    private final boolean encodeRequester;

    public PackageOrderRequestPacket(BlockPos pos, IngredientOrder order, String address, boolean encodeRequester) {
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
//            RedstoneRequesterBlock.programRequester(player, be, order, address);
//            return;
            throw new UnsupportedOperationException("Not yet implemented"); // TODO program requester
        }

        if (!order.isEmpty()) {
            AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(be.getLevel(), pos);
            AllAdvancements.STOCK_TICKER.awardTo(player);
            WiFiEffectPacket.send(player.level(), pos);
        }

        IngredientLogisticsManager.broadcastPackageRequest(be.behaviour.freqId,
                LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, address);
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
        IngredientOrder order = IngredientOrder.read(buf);
        String address = buf.readUtf();
        boolean encodeRequester = buf.readBoolean();
        return new PackageOrderRequestPacket(pos, order, address, encodeRequester);
    }
}
