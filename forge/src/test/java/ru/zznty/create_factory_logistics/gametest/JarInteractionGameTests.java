package ru.zznty.create_factory_logistics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.FactoryItems;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageEntity;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class JarInteractionGameTests {
    private static final BlockPos FLOOR = new BlockPos(0, 0, 0);

    @GameTest(template = "empty", batch = "jar_interactions")
    public static void placingJarCreatesEntityAndConsumesOne(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack held = FactoryItems.REGULAR_JAR.asStack(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos floor = helper.absolutePos(FLOOR);
        Vec3 click = Vec3.atLowerCornerOf(floor).add(0.5, 1, 0.5);
        BlockHitResult hit = new BlockHitResult(click, Direction.UP, floor, false);

        InteractionResult result = held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertValueEqual(result, InteractionResult.SUCCESS, "jar placement result");
        helper.assertValueEqual(held.getCount(), 1, "remaining jar count");
        helper.assertEntitiesPresent(FactoryEntities.JAR.get(), 1);
        JarPackageEntity entity = helper.findOneEntity(FactoryEntities.JAR.get());
        helper.assertTrue(entity.getBox().is(FactoryItems.REGULAR_JAR.get()),
                "placed entity did not retain jar item");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "jar_interactions")
    public static void occupiedJarPositionRejectsSecondPlacement(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack held = FactoryItems.REGULAR_JAR.asStack(2);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos floor = helper.absolutePos(FLOOR);
        Vec3 click = Vec3.atLowerCornerOf(floor).add(0.5, 1, 0.5);
        BlockHitResult hit = new BlockHitResult(click, Direction.UP, floor, false);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);

        helper.assertValueEqual(held.useOn(context), InteractionResult.SUCCESS, "first placement result");
        helper.assertValueEqual(held.useOn(context), InteractionResult.PASS, "overlapping placement result");
        helper.assertValueEqual(held.getCount(), 1, "overlapping placement consumed a jar");
        helper.assertEntitiesPresent(FactoryEntities.JAR.get(), 1);
        helper.succeed();
    }

    private JarInteractionGameTests() {
    }
}
