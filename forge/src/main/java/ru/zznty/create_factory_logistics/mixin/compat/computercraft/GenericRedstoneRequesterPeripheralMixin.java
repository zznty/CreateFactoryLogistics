package ru.zznty.create_factory_logistics.mixin.compat.computercraft;

import com.simibubi.create.compat.computercraft.implementation.peripherals.RedstoneRequesterPeripheral;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.compat.computercraft.GenericDetailsProvider;
import ru.zznty.create_factory_abstractions.compat.computercraft.GenericStackParser;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRedstoneRequester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedMixin")
@Mixin(RedstoneRequesterPeripheral.class)
public abstract class GenericRedstoneRequesterPeripheralMixin extends SyncedPeripheral<RedstoneRequesterBlockEntity> {
    public GenericRedstoneRequesterPeripheralMixin(RedstoneRequesterBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    @Overwrite(remap = false)
    public final void setRequest(IArguments arguments) throws LuaException {
        List<GenericStack> stacks = generateOrder(arguments);
        ((GenericRedstoneRequester) blockEntity).setOrder(GenericOrder.order(stacks));
        blockEntity.notifyUpdate();
    }

    @LuaFunction(mainThread = true)
    @Overwrite(remap = false)
    public final void setCraftingRequest(IArguments arguments) throws LuaException {
        ArrayList<GenericStack> stacks = generateOrder(arguments);

        ArrayList<BigItemStack> craftingPattern = new ArrayList<>();
        for (GenericStack stack : stacks) {
            if (stack.key() instanceof ItemKey key) {
                craftingPattern.add(new BigItemStack(key.stack().copyWithCount(1), 1));
            }
        }

        ((GenericRedstoneRequester) blockEntity).setOrder(GenericOrder.craftingOrder(stacks, craftingPattern));
        blockEntity.notifyUpdate();
    }

    @LuaFunction(mainThread = true)
    @Overwrite(remap = false)
    public final Map<Integer, Map<String, ?>> getRequest() throws LuaException {
        List<GenericStack> stacks = ((GenericRedstoneRequester) blockEntity).getOrder().stacks();
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        for (int i = 0; i < stacks.size(); i++) {
            GenericStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                Map<String, Object> details = new HashMap<>(GenericDetailsProvider.detail(stack));
                details.put("count", stacks.get(i).amount());
                details.put("amount", stacks.get(i).amount());
                result.put(i + 1, details); // +1 because lua
            }
        }
        return result;
    }

    @Unique
    private ArrayList<GenericStack> generateOrder(IArguments arguments) throws LuaException {
        ArrayList<GenericStack> list = new ArrayList<>();

        for (int i = 0; i < arguments.count(); i++) {
            Object arg = arguments.get(i);
            if (arg instanceof String itemName) {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(itemName);
                ItemLike item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                list.add(GenericStack.wrap(new ItemStack(item, 1)));
            } else if (arg instanceof Map<?, ?> data) {
                list.add(GenericStackParser.parseAny(data));
            }
        }

        return list;
    }
}
