package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequesterConfigurationPacket;
import ru.zznty.create_factory_logistics.FactoryPackets;

import java.util.ArrayList;
import java.util.List;

public class RedstoneRequesterConfigurationPacket extends BlockEntityConfigurationPacket<RedstoneRequesterBlockEntity> implements GenericRedstoneRequesterConfigurationPacket {
    private final String address;
    private final boolean allowPartial;
    private final List<GenericStack> stacks;

    public RedstoneRequesterConfigurationPacket(BlockPos pos, String address, boolean allowPartial,
                                                List<GenericStack> stacks) {
        super(pos);
        this.address = address;
        this.allowPartial = allowPartial;
        this.stacks = stacks;
    }

    public RedstoneRequesterConfigurationPacket(BlockPos pos, String address, boolean allowPartial) {
        this(pos, address, allowPartial, new ArrayList<>());
    }

    @Override
    protected void applySettings(ServerPlayer player, RedstoneRequesterBlockEntity be) {
        be.encodedTargetAdress = address;
        be.allowPartialRequests = allowPartial;

        GenericRedstoneRequester requester = (GenericRedstoneRequester) be;
        requester.setOrder(GenericOrder.order(stacks));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return FactoryPackets.CONFIGURE_REDSTONE_REQUESTER;
    }

    @Override
    public List<GenericStack> getStacks() {
        return stacks;
    }

    @Override
    public void setStacks(List<GenericStack> stacks) {
        this.stacks.clear();
        this.stacks.addAll(stacks);
    }

    public static void write(RegistryFriendlyByteBuf buffer, RedstoneRequesterConfigurationPacket packet) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeUtf(packet.address);
        buffer.writeBoolean(packet.allowPartial);
        buffer.writeVarInt(packet.stacks.size());
        for (GenericStack stack : packet.stacks) {
            GenericStackSerializer.write(stack, buffer);
        }
    }

    public static RedstoneRequesterConfigurationPacket read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        String address = buffer.readUtf();
        boolean allowPartial = buffer.readBoolean();
        int size = buffer.readVarInt();
        List<GenericStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(GenericStackSerializer.read(buffer));
        }

        return new RedstoneRequesterConfigurationPacket(pos, address, allowPartial, stacks);
    }
}
