package ru.zznty.create_factory_logistics.logistics.generic;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class NetworkFluidHandlerGameTests {
    @GameTest(template = "empty", batch = "network_capabilities")
    public static void exposesOnlyFluidEntries(GameTestHelper helper) {
        NetworkFluidHandler handler = new NetworkFluidHandler((summary, registries) -> {
            summary.add(FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 2500)));
            summary.add(FluidGenericStack.wrap(new FluidStack(Fluids.LAVA, 750)));
            summary.add(GenericStack.wrap(new ItemStack(Items.STONE, 12)));
        }, helper.getLevel().registryAccess());

        helper.assertValueEqual(handler.getTanks(), 2, "network fluid tank count");
        int water = 0;
        int lava = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack stack = handler.getFluidInTank(tank);
            if (stack.is(Fluids.WATER)) water += stack.getAmount();
            if (stack.is(Fluids.LAVA)) lava += stack.getAmount();
        }
        helper.assertValueEqual(water, 2500, "water amount");
        helper.assertValueEqual(lava, 750, "lava amount");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "network_capabilities")
    public static void simulatesDrainWithoutMutation(GameTestHelper helper) {
        NetworkFluidHandler handler = new NetworkFluidHandler((summary, registries) ->
                summary.add(FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 2500))),
                helper.getLevel().registryAccess());

        FluidStack simulated = handler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        helper.assertTrue(simulated.is(Fluids.WATER), "simulated drain changed fluid");
        helper.assertValueEqual(simulated.getAmount(), 1000, "simulated drain amount");
        helper.assertTrue(handler.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "read-only handler executed a drain");
        helper.assertValueEqual(handler.getFluidInTank(0).getAmount(), 2500, "drain mutated snapshot");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "network_capabilities")
    public static void dividesVirtualCapacityAcrossTanks(GameTestHelper helper) {
        NetworkFluidHandler handler = new NetworkFluidHandler((summary, registries) -> {
            summary.add(FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 1)));
            summary.add(FluidGenericStack.wrap(new FluidStack(Fluids.LAVA, 1)));
        }, helper.getLevel().registryAccess());

        helper.assertValueEqual(handler.getTankCapacity(0), (Integer.MAX_VALUE - 1) / 2,
                "virtual tank capacity");
        helper.assertValueEqual(handler.getTankCapacity(1), (Integer.MAX_VALUE - 1) / 2,
                "virtual tank capacity");
        helper.succeed();
    }

    private NetworkFluidHandlerGameTests() {
    }
}
