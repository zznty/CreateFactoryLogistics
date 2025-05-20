package ru.zznty.create_factory_logistics.logistics.panel.request;

import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ru.zznty.create_factory_logistics.FactoryPackets;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;

import java.util.ArrayList;
import java.util.List;

public class RedstoneRequesterConfigurationPacket extends BlockEntityConfigurationPacket<RedstoneRequesterBlockEntity> implements IngredientRedstoneRequesterConfigurationPacket {
    private final String address;
    private final boolean allowPartial;
    private final List<BigIngredientStack> stacks;

    public RedstoneRequesterConfigurationPacket(BlockPos pos, String address, boolean allowPartial,
                                                List<BigIngredientStack> stacks) {
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

        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) be;
        requester.setOrder(IngredientOrder.order(stacks));
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return FactoryPackets.CONFIGURE_REDSTONE_REQUESTER;
    }

    @Override
    public List<BigIngredientStack> getStacks() {
        return stacks;
    }

    @Override
    public void setStacks(List<BigIngredientStack> stacks) {
        this.stacks.clear();
        this.stacks.addAll(stacks);
    }

    public static void write(RegistryFriendlyByteBuf buffer, RedstoneRequesterConfigurationPacket packet) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeUtf(packet.address);
        buffer.writeBoolean(packet.allowPartial);
        buffer.writeVarInt(packet.stacks.size());
        for (BigIngredientStack stack : packet.stacks) {
            stack.ingredient().write(buffer);
        }
    }

    public static RedstoneRequesterConfigurationPacket read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        String address = buffer.readUtf();
        boolean allowPartial = buffer.readBoolean();
        int size = buffer.readVarInt();
        List<BigIngredientStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(BigIngredientStack.of(BoardIngredient.read(buffer)));
        }

        return new RedstoneRequesterConfigurationPacket(pos, address, allowPartial, stacks);
    }
}
