package ru.zznty.create_factory_logistics.gametest;

import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import net.createmod.catnip.data.Pair;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.mutable.MutableBoolean;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.GenericLogisticsManager;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_abstractions.generic.support.GenericPackagerLinkBlockEntity;
import ru.zznty.create_factory_abstractions.generic.support.GenericRequest;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.util.List;
import java.util.UUID;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class CreateLogisticsNetworkGameTests {
    @GameTest(template = "empty", batch = "create_logistics_network", timeoutTicks = 40)
    public static void linkedPackagerExposesGenericStock(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        GenericStack diamonds = GenericStack.wrap(new ItemStack(Items.DIAMOND, 9));
        var network = LogisticsTestFixture.placeItemNetwork(helper, frequency,
                new ItemStack(Items.DIAMOND, 9));

        helper.runAfterDelay(3, () -> {
            helper.assertTrue(LogisticallyLinkedBehaviour.getAllPresent(frequency, false)
                    .contains(network.link().behaviour), "stock link was not registered");
            GenericInventorySummary summary = GenericInventorySummary.of(
                    network.link().fetchSummaryFromPackager(null));
            helper.assertValueEqual(summary.getCountOf(diamonds.key()), 9,
                    "linked diamond stock");
            helper.assertValueEqual(GenericLogisticsManager.getStockOf(frequency, diamonds, null), 9,
                    "manager diamond stock");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "create_logistics_network", timeoutTicks = 40)
    public static void linkBuildsBoundedGenericRequest(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        GenericStack requested = GenericStack.wrap(new ItemStack(Items.DIAMOND, 12));
        GenericOrder order = GenericOrder.order(List.of(requested));
        var network = LogisticsTestFixture.placeItemNetwork(helper, frequency,
                new ItemStack(Items.DIAMOND, 5));

        helper.runAfterDelay(3, () -> {
            MutableBoolean finalLink = new MutableBoolean(false);
            Pair<PackagerBlockEntity, GenericRequest> pair =
                    ((GenericPackagerLinkBlockEntity) network.link()).processRequest(
                            requested, "warehouse", 2, finalLink, 12345, order, null);
            helper.assertTrue(pair != null, "link did not create a request");
            helper.assertTrue(pair.getFirst() == network.packager(), "wrong packager selected");
            helper.assertValueEqual(pair.getSecond().getCount(), 5, "bounded request count");
            helper.assertValueEqual(pair.getSecond().address(), "warehouse", "request address");
            helper.assertValueEqual(pair.getSecond().context(), order, "request context");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "create_logistics_network", timeoutTicks = 40)
    public static void plannerCreatesPartialRequestForAvailableStock(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        GenericStack diamonds = GenericStack.wrap(new ItemStack(Items.DIAMOND, 12));
        LogisticsTestFixture.placeItemNetwork(helper, frequency, new ItemStack(Items.DIAMOND, 5));

        helper.runAfterDelay(3, () -> {
            Multimap<PackagerBlockEntity, GenericRequest> requests =
                    GenericLogisticsManager.findPackagersForRequest(frequency,
                            GenericOrder.order(List.of(diamonds)), null, "warehouse");
            helper.assertValueEqual(requests.size(), 1, "planned request count");
            helper.assertValueEqual(requests.values().iterator().next().getCount(), 5,
                    "planned partial amount");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "create_logistics_network", timeoutTicks = 40)
    public static void fullBroadcastExtractsPackage(GameTestHelper helper) {
        UUID frequency = UUID.randomUUID();
        GenericStack diamonds = GenericStack.wrap(new ItemStack(Items.DIAMOND, 5));
        var network = LogisticsTestFixture.placeItemNetwork(helper, frequency,
                new ItemStack(Items.DIAMOND, 5));

        helper.runAfterDelay(3, () -> {
            boolean result = GenericLogisticsManager.broadcastPackageRequest(frequency,
                    LogisticallyLinkedBehaviour.RequestType.PLAYER,
                    GenericOrder.order(List.of(diamonds)), null, "warehouse");
            helper.assertTrue(result, "full request was rejected");
            helper.assertTrue(network.storage().getItem(0).isEmpty(), "storage was not extracted");
            helper.assertFalse(network.packager().heldBox.isEmpty(), "packager produced no package");
            helper.succeed();
        });
    }

    private CreateLogisticsNetworkGameTests() {
    }
}
