package ru.zznty.create_factory_logistics.logistics.jar;

import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.FactoryEntities;
import ru.zznty.create_factory_logistics.config.WorldConfig;
import ru.zznty.create_factory_logistics.logistics.abstractions.box.AbstractPackageEntity;
import ru.zznty.create_factory_logistics.logistics.abstractions.box.AbstractPackageItem;
import ru.zznty.create_factory_logistics.logistics.jar.unpack.JarUnpackingHandler;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

import java.util.List;
import java.util.function.Consumer;

public class JarPackageItem extends AbstractPackageItem {

    public JarPackageItem(Properties properties, PackageStyles.PackageStyle style) {
        super(properties, style);
        JarStyles.ALL_JARS.add(this);
    }

    @Override
    protected String getIdSuffix() {
        return "jar";
    }

    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        return JarPackageEntity.fromDroppedItem(world, location, itemstack);
    }

    @Override
    protected EntityEntry<? extends AbstractPackageEntity> getEntityEntry() {
        return FactoryEntities.JAR;
    }

    @Override
    protected InteractionResultHolder<ItemStack> openServerSide(Level worldIn, Player playerIn, InteractionHand handIn,
                                                                ItemStack box, BlockHitResult hitResult) {
        return box.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(fluidItem -> {
            BlockPos relative = hitResult.getBlockPos().relative(hitResult.getDirection());

            FluidStack fluid = fluidItem.drain(WorldConfig.jarCapacity, IFluidHandler.FluidAction.SIMULATE);
            if (fluid.getAmount() != WorldConfig.jarCapacity ||
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
        }).orElse(InteractionResultHolder.pass(box));
    }

    protected boolean canBlockContainFluid(Level worldIn, BlockPos posIn, BlockState blockState, FluidStack fluid) {
        return blockState.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(
                worldIn, posIn, blockState, fluid.getFluid());
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, WorldConfig.jarCapacity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new JarItemRenderer()));
        super.initializeClient(consumer);
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
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        FluidStack contained = FluidUtil.getFluidContained(pStack).orElse(FluidStack.EMPTY);

        if (contained.isEmpty()) return;

        pTooltipComponents.add(contained.getDisplayName()
                                       .copy()
                                       .append(" ")
                                       .append(FactoryFluidPanelBehaviour.formatLevel(
                                               contained.getAmount()).component())
                                       .withStyle(ChatFormatting.GRAY));
    }
}
