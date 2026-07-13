package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericPromiseQueue;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;

import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class GenericPromiseQueueGameTests {
    @GameTest(template = "empty", batch = "generic_promises")
    public static void accountsForItemsFluidsAndComponents(GameTestHelper helper) {
        RequestPromiseQueue queue = new RequestPromiseQueue(() -> {});
        GenericPromiseQueue generic = (GenericPromiseQueue) queue;
        GenericStack water = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1500));
        ItemStack named = new ItemStack(Items.DIAMOND, 3);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("reserved"));
        GenericStack plain = GenericStack.wrap(new ItemStack(Items.DIAMOND, 4));
        GenericStack namedStack = GenericStack.wrap(named);

        generic.add(water);
        generic.add(plain);
        generic.add(namedStack);
        helper.assertValueEqual(generic.getTotalPromisedAndRemoveExpired(water, -1), 1500,
                "water promises");
        helper.assertValueEqual(generic.getTotalPromisedAndRemoveExpired(plain, -1), 4,
                "plain item promises");
        helper.assertValueEqual(generic.getTotalPromisedAndRemoveExpired(namedStack, -1), 3,
                "named item promises");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_promises")
    public static void arrivalConsumesPromises(GameTestHelper helper) {
        RequestPromiseQueue queue = new RequestPromiseQueue(() -> {});
        GenericPromiseQueue generic = (GenericPromiseQueue) queue;
        GenericStack water = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1000));
        generic.add(water.withAmount(600));
        queue.tick();
        generic.add(water.withAmount(700));

        generic.stackEnteredSystem(water.withAmount(800));
        helper.assertValueEqual(generic.getTotalPromisedAndRemoveExpired(water, -1), 500,
                "remaining water promises");
        helper.assertValueEqual(queue.flatten(false).size(), 1, "remaining promise entries");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_promises")
    public static void expiryAndForceClearAreKeySpecific(GameTestHelper helper) {
        RequestPromiseQueue queue = new RequestPromiseQueue(() -> {});
        GenericPromiseQueue generic = (GenericPromiseQueue) queue;
        GenericStack iron = GenericStack.wrap(new ItemStack(Items.IRON_INGOT, 3));
        GenericStack gold = GenericStack.wrap(new ItemStack(Items.GOLD_INGOT, 4));
        generic.add(iron);
        generic.add(gold);
        queue.tick();
        queue.tick();

        helper.assertValueEqual(generic.getTotalPromisedAndRemoveExpired(iron, 2), 0,
                "expired iron promises");
        helper.assertValueEqual(generic.getTotalPromisedAndRemoveExpired(gold, -1), 4,
                "unrelated gold promises");
        generic.forceClear(gold);
        helper.assertTrue(queue.isEmpty(), "force-cleared queue is not empty");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_promises")
    public static void promisesRoundTripThroughNbt(GameTestHelper helper) {
        RequestPromiseQueue queue = new RequestPromiseQueue(() -> {});
        GenericPromiseQueue generic = (GenericPromiseQueue) queue;
        GenericStack diamonds = GenericStack.wrap(new ItemStack(Items.DIAMOND, 7));
        GenericStack water = FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1500));
        generic.add(diamonds);
        generic.add(water);
        queue.tick();
        queue.tick();

        CompoundTag tag = queue.write(helper.getLevel().registryAccess());
        RequestPromiseQueue decoded = RequestPromiseQueue.read(tag, helper.getLevel().registryAccess(), () -> {});
        GenericPromiseQueue decodedGeneric = (GenericPromiseQueue) decoded;
        helper.assertValueEqual(decodedGeneric.getTotalPromisedAndRemoveExpired(diamonds, -1), 7,
                "decoded diamond promises");
        helper.assertValueEqual(decodedGeneric.getTotalPromisedAndRemoveExpired(water, -1), 1500,
                "decoded water promises");
        helper.assertValueEqual(decoded.flatten(false).getFirst().ticksExisted, 2, "decoded age");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_promises")
    public static void emptyPromiseIsIgnored(GameTestHelper helper) {
        AtomicInteger changes = new AtomicInteger();
        RequestPromiseQueue queue = new RequestPromiseQueue(changes::incrementAndGet);
        ((GenericPromiseQueue) queue).add(GenericStack.EMPTY);
        helper.assertTrue(queue.isEmpty(), "empty promise made queue nonempty");
        helper.assertTrue(queue.flatten(false).isEmpty(), "empty promise was retained");
        helper.assertValueEqual(changes.get(), 0, "empty promise fired callback");
        helper.succeed();
    }

    private GenericPromiseQueueGameTests() {
    }
}
