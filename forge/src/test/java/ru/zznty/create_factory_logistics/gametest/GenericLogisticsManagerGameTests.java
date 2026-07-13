package ru.zznty.create_factory_logistics.gametest;

import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.List;
import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class GenericLogisticsManagerGameTests {
    @GameTest(template = "empty", batch = "generic_requests")
    public static void emptyOrderIsRejected(GameTestHelper helper) {
        boolean result = GenericLogisticsManager.broadcastPackageRequest(UUID.randomUUID(),
                LogisticallyLinkedBehaviour.RequestType.PLAYER, GenericOrder.empty(), null, "destination");
        helper.assertFalse(result, "empty order was accepted");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_requests")
    public static void plannerFindsNoPackagersWithoutLinks(GameTestHelper helper) {
        GenericOrder order = GenericOrder.order(List.of(
                GenericStack.wrap(new ItemStack(Items.DIAMOND, 4))));
        Multimap<PackagerBlockEntity, GenericRequest> requests =
                GenericLogisticsManager.findPackagersForRequest(UUID.randomUUID(), order, null, "destination");
        helper.assertTrue(requests.isEmpty(), "planner found a nonexistent packager");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "generic_requests")
    public static void unfulfilledOrderIsRejected(GameTestHelper helper) {
        GenericOrder order = GenericOrder.order(List.of(
                GenericStack.wrap(new ItemStack(Items.DIAMOND, 4))));
        boolean result = GenericLogisticsManager.broadcastPackageRequest(UUID.randomUUID(),
                LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, "destination");
        helper.assertFalse(result, "unfulfilled request reported success");
        helper.succeed();
    }

    private GenericLogisticsManagerGameTests() {
    }
}
