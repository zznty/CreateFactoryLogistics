package ru.zznty.create_factory_logistics.gametest;

import io.netty.buffer.Unpooled;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.connection.ConnectionType;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackageMeasureResult;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalKey;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalKeySerializer;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.NetworkChemicalHandler;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel.BarrelPackageItem;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrelPackager.BarrelPackageBuilder;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class MekanismGameTests {
    @GameTest(template = "empty", batch = "mekanism")
    public static void chemicalKeyRoundTrips(GameTestHelper helper) {
        ChemicalKeySerializer serializer = new ChemicalKeySerializer();
        ChemicalKey expected = hydrogen();
        CompoundTag tag = new CompoundTag();
        serializer.write(expected, helper.getLevel().registryAccess(), tag);
        helper.assertValueEqual(serializer.read(helper.getLevel().registryAccess(), tag), expected,
                "chemical key NBT round trip");

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                helper.getLevel().registryAccess(), ConnectionType.NEOFORGE);
        try {
            serializer.write(expected, buffer);
            helper.assertValueEqual(serializer.read(buffer), expected, "chemical key network round trip");
            helper.assertValueEqual(buffer.readableBytes(), 0, "unread chemical key bytes");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "mekanism")
    public static void unknownChemicalDecodesAsEmpty(GameTestHelper helper) {
        ChemicalKeySerializer serializer = new ChemicalKeySerializer();
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "create_factory_logistics:not_a_chemical");
        ChemicalKey decoded = serializer.read(helper.getLevel().registryAccess(), tag);
        helper.assertValueEqual(decoded.chemical(), MekanismAPI.EMPTY_CHEMICAL_HOLDER,
                "unknown chemical fallback");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "mekanism")
    public static void barrelBuilderCapsChemical(GameTestHelper helper) {
        BarrelPackageBuilder builder = new BarrelPackageBuilder();
        int capacity = Math.toIntExact(ChemicalTankTier.BASIC.getStorage());
        helper.assertValueEqual(builder.slotCount(), 1, "barrel slot count");
        helper.assertValueEqual(builder.maxPerSlot(), capacity, "barrel capacity");
        helper.assertValueEqual(builder.measure(hydrogen()), PackageMeasureResult.BULKY,
                "chemical package measurement");
        helper.assertValueEqual(builder.add(new GenericStack(hydrogen(), capacity + 500)), 500,
                "barrel overflow remainder");
        helper.assertTrue(builder.isFull(), "barrel builder is not full");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "mekanism")
    public static void barrelBuilderRejectsDifferentChemical(GameTestHelper helper) {
        BarrelPackageBuilder builder = new BarrelPackageBuilder();
        helper.assertValueEqual(builder.add(new GenericStack(hydrogen(), 500)), 0,
                "initial chemical remainder");
        helper.assertValueEqual(builder.add(new GenericStack(oxygen(), 250)), -1,
                "different chemical rejection");
        helper.assertValueEqual(builder.content().getFirst().key(), hydrogen(), "retained chemical");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "mekanism")
    public static void builtBarrelContainsChemical(GameTestHelper helper) {
        BarrelPackageBuilder builder = new BarrelPackageBuilder();
        builder.add(new GenericStack(hydrogen(), 1000));
        ItemStack barrel = builder.build();
        helper.assertTrue(barrel.getItem() instanceof BarrelPackageItem, "builder output is not a barrel");
        IChemicalHandler handler = barrel.getCapability(Capabilities.CHEMICAL.item());
        helper.assertTrue(handler != null, "barrel has no chemical capability");
        ChemicalStack stored = handler.getChemicalInTank(0);
        helper.assertTrue(stored.is(MekanismChemicals.HYDROGEN), "built barrel contains wrong chemical");
        helper.assertValueEqual(stored.getAmount(), 1000L, "built barrel amount");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "mekanism")
    public static void networkChemicalHandlerSimulatesExtraction(GameTestHelper helper) {
        NetworkChemicalHandler handler = new NetworkChemicalHandler((summary, registries) ->
                summary.add(new GenericStack(hydrogen(), 2500)), helper.getLevel().registryAccess());
        ChemicalStack simulated = handler.extractChemical(0, 1000, Action.SIMULATE);
        helper.assertTrue(simulated.is(MekanismChemicals.HYDROGEN), "simulated extraction changed chemical");
        helper.assertValueEqual(simulated.getAmount(), 1000L, "simulated chemical extraction amount");
        helper.assertTrue(handler.extractChemical(0, 1000, Action.EXECUTE).isEmpty(),
                "read-only chemical handler executed extraction");
        helper.succeed();
    }

    private static ChemicalKey hydrogen() {
        return new ChemicalKey(MekanismChemicals.HYDROGEN);
    }

    private static ChemicalKey oxygen() {
        return new ChemicalKey(MekanismChemicals.OXYGEN);
    }

    private MekanismGameTests() {
    }
}
