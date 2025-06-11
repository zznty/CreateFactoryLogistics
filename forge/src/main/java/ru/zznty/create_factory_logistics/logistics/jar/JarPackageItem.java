package ru.zznty.create_factory_logistics.logistics.jar;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.Config;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.logistics.jar.unpack.JarUnpackingHandler;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

public class JarPackageItem extends PackageItem {

    public JarPackageItem(Properties properties, PackageStyles.PackageStyle style) {
        super(properties, style);

        // we don't want our jars appearing from other packaging appliances
        PackageStyles.ALL_BOXES.remove(this);
        (style.rare() ? PackageStyles.RARE_BOXES : PackageStyles.STANDARD_BOXES).remove(this);

        JarStyles.ALL_JARS.add(this);
    }

    @Override
    public String getDescriptionId() {
        return "item." + CreateFactoryLogistics.MODID + (style.rare() ? ".rare_jar" : ".jar");
    }

    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        return JarPackageEntity.fromDroppedItem(world, location, itemstack);
    }

    @Override
    public InteractionResultHolder<ItemStack> open(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack box = playerIn.getItemInHand(handIn);

        BlockHitResult hitResult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.NONE);

        if (hitResult.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(box);

        BlockPos relative = hitResult.getBlockPos().relative(hitResult.getDirection());

        IFluidHandlerItem fluidItem = box.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidItem == null) return InteractionResultHolder.pass(box);

        FluidStack fluid = fluidItem.drain(Config.jarCapacity, IFluidHandler.FluidAction.SIMULATE);
        if (fluid.getAmount() != Config.jarCapacity ||
                !(fluid.getFluid() instanceof FlowingFluid) ||
                !worldIn.mayInteract(playerIn, relative) ||
                !playerIn.mayUseItemAt(relative, hitResult.getDirection(), box) ||
                !(worldIn instanceof ServerLevel serverLevel))
            return InteractionResultHolder.fail(box);

        JarUnpackingHandler handler = JarUnpackingHandler.REGISTRY.get(fluid.getFluid());
        boolean success;
        if (handler != null) {
            success = handler.unpack(serverLevel, relative, fluid, playerIn);
        } else {
            BlockState blockState = worldIn.getBlockState(hitResult.getBlockPos());

            BlockPos placePos = canBlockContainFluid(worldIn, hitResult.getBlockPos(), blockState, fluid) ?
                                hitResult.getBlockPos() :
                                relative;

            success = emptyContents(playerIn, worldIn, handIn, placePos, box);
        }

        if (success) {
            playerIn.setItemInHand(handIn, ItemStack.EMPTY);
            return InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, worldIn.isClientSide());
        }

        return InteractionResultHolder.fail(box);
    }

    protected boolean canBlockContainFluid(Level worldIn, BlockPos posIn, BlockState blockState, FluidStack fluid) {
        return blockState.getBlock() instanceof LiquidBlockContainer liquidBlockContainer &&
                liquidBlockContainer.canPlaceLiquid(null, worldIn, posIn, blockState, fluid.getFluid());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer()
                .isShiftKeyDown()) {
            return open(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        }

        Vec3 point = context.getClickLocation();
        float h = style.height() / 16f;
        float r = style.width() / 2f / 16f;

        if (context.getClickedFace() == Direction.DOWN)
            point = point.subtract(0, h + .25f, 0);
        else if (context.getClickedFace()
                .getAxis()
                .isHorizontal())
            point = point.add(Vec3.atLowerCornerOf(context.getClickedFace()
                                                           .getNormal())
                                      .scale(r));

        AABB scanBB = new AABB(point, point).inflate(r, 0, r)
                .expandTowards(0, h, 0);
        Level world = context.getLevel();
        if (!world.getEntities(FactoryEntities.JAR.get(), scanBB, e -> true)
                .isEmpty())
            return InteractionResult.PASS;

        JarPackageEntity jarEntity = new JarPackageEntity(world, point.x, point.y, point.z);
        ItemStack itemInHand = context.getItemInHand();
        jarEntity.setBox(itemInHand.copy());
        world.addFreshEntity(jarEntity);
        itemInHand.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int ticks) {
        if (!(entity instanceof Player player))
            return;
        int i = this.getUseDuration(stack, entity) - ticks;
        if (i < 0)
            return;

        float f = getPackageVelocity(i);
        if (f < 0.1D)
            return;
        if (world.isClientSide)
            return;

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                        SoundSource.NEUTRAL, 0.5F, 0.5F);

        ItemStack copy = stack.copy();
        if (!player.getAbilities().instabuild)
            stack.shrink(1);

        Vec3 vec = new Vec3(entity.getX(), entity.getY() + entity.getBoundingBox()
                .getYsize() / 2f, entity.getZ());
        Vec3 motion = entity.getLookAngle()
                .scale(f * 2);
        vec = vec.add(motion);

        JarPackageEntity jarEntity = new JarPackageEntity(world, vec.x, vec.y, vec.z);
        jarEntity.setBox(copy);
        jarEntity.setDeltaMovement(motion);
        jarEntity.tossedBy = new WeakReference<>(player);
        world.addFreshEntity(jarEntity);
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new JarItemRenderer()));
        super.initializeClient(consumer);
    }

    public static ItemStack slurp(Level world, BlockPos pos, IFluidHandler tank, FluidStack extractedFluid,
                                  int amount) {
        if (amount < 1) amount = Config.jarCapacity;

        ItemStack jar = new ItemStack(JarStyles.getRandomJar());

        FluidActionResult result = FluidUtil.tryFillContainer(jar, tank, amount, null, true);

        if (!result.isSuccess())
            return ItemStack.EMPTY;

        world.playSound(null, pos, FluidHelper.getFillSound(extractedFluid), SoundSource.BLOCKS, .5f, 1);

        return result.getResult();
    }

    public static ItemStack getDefaultJar() {
        return JarStyles.ALL_JARS.get(0).getDefaultInstance();
    }

    public boolean emptyContents(@Nullable Player player, Level level, InteractionHand hand, BlockPos pos,
                                 ItemStack container) {
        FluidStack resource = FluidUtil.getFluidContained(container).orElse(FluidStack.EMPTY);
        if (resource.getAmount() < 1000)
            return false;

        return FluidUtil.tryPlaceFluid(player, level, hand, pos, container, resource).isSuccess();
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, tooltipContext, pTooltipComponents, pIsAdvanced);

        FluidStack contained = FluidUtil.getFluidContained(pStack).orElse(FluidStack.EMPTY);

        if (contained.isEmpty()) return;

        pTooltipComponents.add(contained.getHoverName()
                                       .copy()
                                       .append(" ")
                                       .append(FactoryFluidPanelBehaviour.formatLevel(
                                               contained.getAmount()).component())
                                       .withStyle(ChatFormatting.GRAY));
    }
}
