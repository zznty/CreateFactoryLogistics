package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.connection.ConnectionType;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKeySerializer;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKeySerializer;

import java.util.List;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class GenericSerializationGameTests {
    @GameTest(template = "empty", batch = "serialization")
    public static void itemGenericStackPreservesComponents(GameTestHelper helper) {
        ItemStack source = new ItemStack(Items.DIAMOND, 7);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("named diamond"));

        GenericStack wrapped = GenericStack.wrap(source);
        helper.assertValueEqual(wrapped.amount(), 7, "wrapped amount");
        helper.assertTrue(wrapped.key() instanceof ItemKey, "wrapped key is not an item key");

        ItemStack keyStack = ((ItemKey) wrapped.key()).stack();
        helper.assertValueEqual(keyStack.getCount(), 1, "item key count");
        helper.assertTrue(ItemStack.isSameItemSameComponents(source, keyStack), "item components changed");
        helper.assertValueEqual(wrapped.withAmount(19).amount(), 19, "withAmount result");
        helper.assertValueEqual(wrapped.amount(), 7, "withAmount mutated the source");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "serialization")
    public static void itemKeyRoundTrips(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        ItemStack source = new ItemStack(Items.NETHERITE_PICKAXE);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("serializer test"));
        ItemKey expected = new ItemKey(source);
        ItemKeySerializer serializer = new ItemKeySerializer();

        CompoundTag tag = new CompoundTag();
        serializer.write(expected, registries, tag);
        helper.assertValueEqual(serializer.read(registries, tag), expected, "item key NBT round trip");

        RegistryFriendlyByteBuf buffer = newBuffer(helper);
        try {
            serializer.write(expected, buffer);
            helper.assertValueEqual(serializer.read(buffer), expected, "item key network round trip");
            helper.assertValueEqual(buffer.readableBytes(), 0, "unread item key bytes");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "serialization")
    public static void fluidKeyRoundTrips(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        FluidStack source = new FluidStack(Fluids.WATER, 810);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("process water"));
        FluidKey expected = (FluidKey) FluidGenericStack.wrap(source).key();
        FluidKeySerializer serializer = new FluidKeySerializer();

        CompoundTag tag = new CompoundTag();
        serializer.write(expected, registries, tag);
        FluidKey fromNbt = serializer.read(registries, tag);
        helper.assertTrue(FluidStack.isSameFluidSameComponents(expected.stack(), fromNbt.stack()),
                "fluid key NBT round trip changed components");

        RegistryFriendlyByteBuf buffer = newBuffer(helper);
        try {
            serializer.write(expected, buffer);
            FluidKey fromNetwork = serializer.read(buffer);
            helper.assertTrue(FluidStack.isSameFluidSameComponents(expected.stack(), fromNetwork.stack()),
                    "fluid key network round trip changed components");
            helper.assertValueEqual(buffer.readableBytes(), 0, "unread fluid key bytes");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "serialization")
    public static void genericOrderRoundTrips(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        GenericStack item = GenericStack.wrap(new ItemStack(Items.COPPER_INGOT, 34));
        GenericStack fluid = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1500));
        BigItemStack craftItem = BigGenericStack.of(item.withAmount(3)).asStack();
        BigItemStack craftFluid = BigGenericStack.of(fluid.withAmount(250)).asStack();
        GenericOrder expected = new GenericOrder(List.of(item, fluid),
                List.of(new PackageOrderWithCrafts.CraftingEntry(
                        new PackageOrder(List.of(craftItem, craftFluid)), 4)));

        helper.assertValueEqual(GenericOrder.read(registries, expected.write(registries)), expected,
                "generic order NBT round trip");

        RegistryFriendlyByteBuf buffer = newBuffer(helper);
        try {
            expected.write(buffer);
            helper.assertValueEqual(GenericOrder.read(buffer), expected, "generic order network round trip");
            helper.assertValueEqual(buffer.readableBytes(), 0, "unread generic order bytes");
        } finally {
            buffer.release();
        }
        helper.assertValueEqual(GenericOrder.of(expected.asCrafting()), expected, "Create order conversion");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "serialization")
    public static void emptyGenericStackRoundTripsThroughNbt(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        GenericStackSerializer.write(helper.getLevel().registryAccess(), GenericStack.EMPTY, tag);
        helper.assertValueEqual(GenericStackSerializer.read(helper.getLevel().registryAccess(), tag),
                GenericStack.EMPTY, "empty generic stack NBT round trip");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "serialization")
    public static void emptyFluidKeyCanBeWrittenAgain(GameTestHelper helper) {
        FluidKeySerializer serializer = new FluidKeySerializer();
        FluidKey decoded = serializer.read(helper.getLevel().registryAccess(), new CompoundTag());
        helper.assertTrue(decoded.stack().isEmpty(), "empty fluid key did not decode as empty");
        serializer.write(decoded, helper.getLevel().registryAccess(), new CompoundTag());
        helper.succeed();
    }

    private static RegistryFriendlyByteBuf newBuffer(GameTestHelper helper) {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess(),
                ConnectionType.NEOFORGE);
    }

    private GenericSerializationGameTests() {
    }
}
