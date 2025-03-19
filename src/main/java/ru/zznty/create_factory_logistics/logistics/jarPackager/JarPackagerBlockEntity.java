package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.FluidBoardIngredient;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JarPackagerBlockEntity extends PackagerBlockEntity {
    private TankManipulationBehaviour drainInventory;

    public JarPackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        behaviours.add(drainInventory = new TankManipulationBehaviour(this, CapManipulationBehaviourBase.InterfaceProvider.oppositeOfBlockFacing())
                .withFilter(this::supportsBlockEntity));
    }

    private boolean supportsBlockEntity(BlockEntity target) {
        return target != null && !(target instanceof PortableStorageInterfaceBlockEntity);
    }

    @Override
    public InventorySummary getAvailableItems(boolean scanInputSlots) {
        if (!drainInventory.hasInventory()) return InventorySummary.EMPTY;

        IFluidHandler fluidHandler = drainInventory.getInventory();

        InventorySummary summary = new InventorySummary();

        IFluidInventorySummary fluidSummary = (IFluidInventorySummary) summary;

        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack stack = fluidHandler.getFluidInTank(i);
            if (!stack.isEmpty()) {
                if (fluidHandler instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
                    stack.setAmount(BigItemStack.INF);

                fluidSummary.add(stack);
            }
        }

        return summary;
    }

    public ItemStack extractJar(BoardIngredient ingredient) {
        if (ingredient != BoardIngredient.EMPTY && !(ingredient instanceof FluidBoardIngredient))
            throw new IllegalStateException("Unsupported board ingredient: " + ingredient);

        // todo replace with fluid handler call to support multi-tank blocks
        FluidStack extractedFluid = drainInventory.simulate().extractAny();

        if (extractedFluid == FluidStack.EMPTY ||
                (ingredient instanceof FluidBoardIngredient fluidIngredient &&
                        (extractedFluid.getFluid() != fluidIngredient.stack().getFluid() ||
                                extractedFluid.getAmount() < fluidIngredient.stack().getAmount())))
            return ItemStack.EMPTY;

        return JarPackageItem.slurp(getLevel(), getBlockPos(), Objects.requireNonNull(drainInventory.getInventory()), ingredient.amount());
    }

    @Override
    public void attemptToSend(List<PackagingRequest> queuedRequests) {
        if (queuedRequests == null && (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0))
            return;

        ItemStack createdBox = extractJar(BoardIngredient.EMPTY);

        if (createdBox.isEmpty())
            return;

        PackageItem.clearAddress(createdBox);

        if (!signBasedAddress.isBlank())
            PackageItem.addAddress(createdBox, signBasedAddress);

        // todo package links support for jars
        /*BlockPos linkPos = getLinkPos();
        if (linkPos != null
                && level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity plbe)
            plbe.behaviour.deductFromAccurateSummary(extractedItems);*/

        if (!heldBox.isEmpty() || animationTicks != 0) {
            queuedExitingPackages.add(new BigItemStack(createdBox));
            return;
        }

        heldBox = createdBox;
        animationInward = false;
        animationTicks = CYCLE;

        triggerStockCheck();
        notifyUpdate();
    }

    @Override
    public boolean unwrapBox(ItemStack box, boolean simulate) {
        if (animationTicks > 0)
            return false;

        Objects.requireNonNull(this.level);

        if (!(box.getItem() instanceof JarPackageItem))
            return false;

        Optional<FluidStack> containedFluid = FluidUtil.getFluidContained(box);

        if (containedFluid.isEmpty())
            return false;

        Direction facing = getBlockState().getOptionalValue(PackagerBlock.FACING).orElse(Direction.UP);
        BlockPos target = worldPosition.relative(facing.getOpposite());

        ItemStack originalBox = box.copy();

        boolean unpacked = FluidUtil.getFluidHandler(level, target, facing).map(fluidHandler ->
                        !FluidUtil.tryFluidTransfer(fluidHandler, FluidUtil.getFluidHandler(box).resolve().get(), containedFluid.get(), !simulate).isEmpty())
                .orElse(false);

        if (unpacked && !simulate) {
            level.playSound(null, worldPosition, FluidHelper.getEmptySound(containedFluid.get()), SoundSource.BLOCKS, .5f, 1);

            previouslyUnwrapped = originalBox;
            animationInward = true;
            animationTicks = CYCLE;
            notifyUpdate();
        }

        return unpacked;
    }
}
