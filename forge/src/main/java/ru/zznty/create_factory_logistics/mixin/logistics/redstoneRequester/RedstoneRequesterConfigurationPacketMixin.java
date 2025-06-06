package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterConfigurationPacket;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequesterConfigurationPacket;

import java.util.ArrayList;
import java.util.List;

@Mixin(RedstoneRequesterConfigurationPacket.class)
public class RedstoneRequesterConfigurationPacketMixin implements GenericRedstoneRequesterConfigurationPacket {
    @Shadow(remap = false)
    private String address;
    @Shadow(remap = false)
    private boolean allowPartial;

    @Unique
    private List<GenericStack> createFactoryLogistics$stacks;

    @Overwrite(remap = false)
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeUtf(address);
        buffer.writeBoolean(allowPartial);
        buffer.writeVarInt(createFactoryLogistics$stacks.size());
        for (GenericStack stack : createFactoryLogistics$stacks) {
            GenericStackSerializer.write(stack, buffer);
        }
    }

    @Overwrite(remap = false)
    protected void readSettings(FriendlyByteBuf buffer) {
        address = buffer.readUtf();
        allowPartial = buffer.readBoolean();
        int size = buffer.readVarInt();
        createFactoryLogistics$stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            createFactoryLogistics$stacks.add(GenericStackSerializer.read(buffer));
        }
    }

    @Overwrite(remap = false)
    protected void applySettings(RedstoneRequesterBlockEntity be) {
        be.encodedTargetAdress = address;
        be.allowPartialRequests = allowPartial;

        GenericRedstoneRequester requester = (GenericRedstoneRequester) be;
        requester.setOrder(GenericOrder.order(createFactoryLogistics$stacks));
    }

    @Override
    public List<GenericStack> getStacks() {
        return createFactoryLogistics$stacks == null ? List.of() : createFactoryLogistics$stacks;
    }

    @Override
    public void setStacks(List<GenericStack> stacks) {
        createFactoryLogistics$stacks = stacks;
    }
}
