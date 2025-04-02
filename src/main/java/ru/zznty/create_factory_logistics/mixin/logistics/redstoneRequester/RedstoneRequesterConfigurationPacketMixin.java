package ru.zznty.create_factory_logistics.mixin.logistics.redstoneRequester;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterConfigurationPacket;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_logistics.logistics.ingredient.BigIngredientStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientOrder;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRedstoneRequester;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientRedstoneRequesterConfigurationPacket;

import java.util.ArrayList;
import java.util.List;

@Mixin(RedstoneRequesterConfigurationPacket.class)
public class RedstoneRequesterConfigurationPacketMixin implements IngredientRedstoneRequesterConfigurationPacket {
    @Shadow(remap = false)
    private String address;
    @Shadow(remap = false)
    private boolean allowPartial;

    @Unique
    private List<BigIngredientStack> createFactoryLogistics$stacks;

    @Overwrite(remap = false)
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeUtf(address);
        buffer.writeBoolean(allowPartial);
        buffer.writeVarInt(createFactoryLogistics$stacks.size());
        for (BigIngredientStack stack : createFactoryLogistics$stacks) {
            stack.asStack().send(buffer);
        }
    }

    @Overwrite(remap = false)
    protected void readSettings(FriendlyByteBuf buffer) {
        address = buffer.readUtf();
        allowPartial = buffer.readBoolean();
        int size = buffer.readVarInt();
        createFactoryLogistics$stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            createFactoryLogistics$stacks.add((BigIngredientStack) BigItemStack.receive(buffer));
        }
    }

    @Overwrite(remap = false)
    protected void applySettings(RedstoneRequesterBlockEntity be) {
        be.encodedTargetAdress = address;
        be.allowPartialRequests = allowPartial;

        IngredientRedstoneRequester requester = (IngredientRedstoneRequester) be;
        requester.setOrder(IngredientOrder.order(createFactoryLogistics$stacks));
    }

    @Override
    public List<BigIngredientStack> getStacks() {
        return createFactoryLogistics$stacks == null ? List.of() : createFactoryLogistics$stacks;
    }

    @Override
    public void setStacks(List<BigIngredientStack> stacks) {
        createFactoryLogistics$stacks = stacks;
    }
}
