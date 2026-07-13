package ru.zznty.create_factory_logistics.gametest;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;

import java.util.List;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class GenericOrderFragmentGameTests {
    @GameTest(template = "empty", batch = "package_fragments")
    public static void fragmentPreservesGenericOrderAndFlags(GameTestHelper helper) {
        GenericOrder expected = GenericOrder.craftingOrder(
                List.of(GenericStack.wrap(new ItemStack(Items.OAK_PLANKS, 4)),
                        FluidGenericStack.wrap(new FluidStack(Fluids.WATER, 250))),
                List.of(new BigItemStack(new ItemStack(Items.OAK_PLANKS), 1)));
        ItemStack box = PackageItem.containing(List.of(new ItemStack(Items.OAK_PLANKS)));
        GenericOrder.set(helper.getLevel().registryAccess(), box, 31, 2, true, 4, true, expected);

        helper.assertValueEqual(PackageItem.getOrderId(box), 31, "order id");
        helper.assertValueEqual(GenericOrder.of(helper.getLevel().registryAccess(), box), expected,
                "fragment order context");
        CompoundTag fragment = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getCompound("Fragment");
        helper.assertValueEqual(fragment.getInt("LinkIndex"), 2, "link index");
        helper.assertValueEqual(fragment.getInt("Index"), 4, "fragment index");
        helper.assertTrue(fragment.getBoolean("IsFinalLink"), "final-link flag");
        helper.assertTrue(fragment.getBoolean("IsFinal"), "final-fragment flag");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "package_fragments")
    public static void packageWithoutContextReturnsNull(GameTestHelper helper) {
        ItemStack box = PackageItem.containing(List.of(new ItemStack(Items.IRON_INGOT)));
        helper.assertValueEqual(PackageItem.getOrderId(box), -1, "plain package order id");
        helper.assertTrue(GenericOrder.of(helper.getLevel().registryAccess(), box) == null,
                "plain package has order context");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "package_fragments")
    public static void craftOnlyOrderIsNotEmpty(GameTestHelper helper) {
        GenericOrder order = GenericOrder.of(PackageOrderWithCrafts.singleRecipe(
                List.of(new BigItemStack(new ItemStack(Items.OAK_PLANKS), 1))));
        helper.assertFalse(order.isEmpty(), "craft-only order was treated as empty");
        helper.assertValueEqual(order.crafts().size(), 1, "craft entry count");
        helper.succeed();
    }

    private GenericOrderFragmentGameTests() {
    }
}
