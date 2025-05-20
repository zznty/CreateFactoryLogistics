package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import ru.zznty.create_factory_logistics.FactoryPackets;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

import java.util.ArrayList;
import java.util.List;

public record LogisticalStockResponsePacket(boolean lastPacket, BlockPos pos,
                                            List<BigItemStack> stacks) implements ClientboundPacketPayload {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof StockTickerBlockEntity stbe)
            stbe.receiveStockPacket(stacks, lastPacket);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return FactoryPackets.LOGISTICAL_STOCK_RESPONSE;
    }

    public static void write(RegistryFriendlyByteBuf buf, LogisticalStockResponsePacket packet) {
        buf.writeBoolean(packet.lastPacket);
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.stacks.size());
        for (BigItemStack stack : packet.stacks) {
            ((BigIngredientStack) stack).ingredient().write(buf);
        }
    }

    public static LogisticalStockResponsePacket read(RegistryFriendlyByteBuf buf) {
        boolean lastPacket = buf.readBoolean();
        BlockPos pos = buf.readBlockPos();
        int count = buf.readVarInt();
        List<BigItemStack> stacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            stacks.add(BigIngredientStack.of(BoardIngredient.read(buf)).asStack());
        }
        return new LogisticalStockResponsePacket(lastPacket, pos, stacks);
    }
}
