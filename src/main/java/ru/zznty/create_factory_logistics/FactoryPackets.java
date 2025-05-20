package ru.zznty.create_factory_logistics;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import ru.zznty.create_factory_logistics.logistics.panel.request.LogisticalStockResponsePacket;
import ru.zznty.create_factory_logistics.logistics.panel.request.PackageOrderRequestPacket;
import ru.zznty.create_factory_logistics.logistics.panel.request.RedstoneRequesterConfigurationPacket;

import java.util.Locale;

// usually i dont like copying and pasting, but fuck codecs
public enum FactoryPackets implements BasePacketPayload.PacketTypeProvider {
    LOGISTICAL_STOCK_RESPONSE(LogisticalStockResponsePacket.class,
            StreamCodec.of(LogisticalStockResponsePacket::write, LogisticalStockResponsePacket::read)),
    CONFIGURE_REDSTONE_REQUESTER(RedstoneRequesterConfigurationPacket.class,
            StreamCodec.of(RedstoneRequesterConfigurationPacket::write, RedstoneRequesterConfigurationPacket::read)),
    LOGISTICS_PACKAGE_REQUEST(PackageOrderRequestPacket.class,
            StreamCodec.of(PackageOrderRequestPacket::write, PackageOrderRequestPacket::read));
    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> FactoryPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(CreateFactoryLogistics.resource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CreateFactoryLogistics.MODID, 1);
        for (FactoryPackets packet : values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }
}
