package ru.zznty.create_factory_logistics.logistics.jarPackager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.TankManipulationBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.request.*;
import ru.zznty.create_factory_logistics.logistics.stock.IIngredientInventorySummary;

import java.util.*;

public class JarPackagerBlockEntity extends PackagerBlockEntity {
    private TankManipulationBehaviour drainInventory;
    private IIngredientInventorySummary available;

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
        if (!drainInventory.hasInventory()) {
            // in case inventory didn't load in the first tick
            drainInventory.findNewCapability();
            if (!drainInventory.hasInventory())
                return InventorySummary.EMPTY;
        }

        IFluidHandler fluidHandler = drainInventory.getInventory();

        InventorySummary summary = new InventorySummary();

        IIngredientInventorySummary fluidSummary = (IIngredientInventorySummary) summary;

        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack stack = fluidHandler.getFluidInTank(i);
            if (!stack.isEmpty()) {
                if (!scanInputSlots)
                    stack = fluidHandler.drain(stack, IFluidHandler.FluidAction.SIMULATE);
                if (fluidHandler instanceof CreativeFluidTankBlockEntity.CreativeSmartFluidTank)
                    stack.setAmount(BigItemStack.INF);

                fluidSummary.add(stack);
            }
        }

        if (!level.isClientSide)
            submitNewArrivals(available, fluidSummary);
        available = fluidSummary;

        return summary;
    }

    private void submitNewArrivals(IIngredientInventorySummary before, IIngredientInventorySummary after) {
        if (before == null || after.isEmpty())
            return;

        Set<IngredientPromiseQueue> promiseQueues = collectAdjacentQueues();

        if (promiseQueues.isEmpty())
            return;

        for (BigIngredientStack stack : after.getStacks()) {
            before.add(stack.getIngredient(), -stack.getCount());
        }

        for (IngredientPromiseQueue queue : promiseQueues) {
            for (BigIngredientStack stack : before.getStacks()) {
                if (stack.getCount() < 0)
                    queue.ingredientEnteredSystem(stack.getIngredient().withAmount(-stack.getCount()));
            }
        }
    }

    private Set<IngredientPromiseQueue> collectAdjacentQueues() {
        Set<IngredientPromiseQueue> promiseQueues = new HashSet<>();

        Objects.requireNonNull(level);

        for (Direction d : Iterate.directions) {
            if (!level.isLoaded(worldPosition.relative(d)))
                continue;

            BlockState adjacentState = level.getBlockState(worldPosition.relative(d));
            if (FactoryBlocks.FACTORY_FLUID_GAUGE.has(adjacentState)) {
                if (FactoryPanelBlock.connectedDirection(adjacentState) != d)
                    continue;
                if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof FactoryPanelBlockEntity fpbe))
                    continue;
                if (!fpbe.restocker)
                    continue;
                for (FactoryPanelBehaviour behaviour : fpbe.panels.values()) {
                    if (!behaviour.isActive())
                        continue;
                    promiseQueues.add((IngredientPromiseQueue) behaviour.restockerPromises);
                }
            }

            if (AllBlocks.STOCK_LINK.has(adjacentState)) {
                if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                    continue;
                if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof PackagerLinkBlockEntity plbe))
                    continue;
                UUID freqId = plbe.behaviour.freqId;
                if (!Create.LOGISTICS.hasQueuedPromises(freqId))
                    continue;
                promiseQueues.add((IngredientPromiseQueue) Create.LOGISTICS.getQueuedPromises(freqId));
            }
        }

        return promiseQueues;
    }

    public ItemStack extractJar(BoardIngredient ingredient) {
        if (!drainInventory.hasInventory()) return ItemStack.EMPTY;

        FluidBoardIngredient fluidIngredient;
        if (ingredient == BoardIngredient.EMPTY) {
            FluidStack containedFluid = drainInventory.getInventory().drain(JarPackageItem.JAR_CAPACITY, IFluidHandler.FluidAction.SIMULATE);
            if (containedFluid.isEmpty())
                return ItemStack.EMPTY;
            fluidIngredient = new FluidBoardIngredient(containedFluid, containedFluid.getAmount());
        } else if (ingredient instanceof FluidBoardIngredient) {
            fluidIngredient = (FluidBoardIngredient) ingredient;
        } else {
            throw new IllegalStateException("Unsupported board ingredient: " + ingredient);
        }

        FluidStack extractedFluid = drainInventory.getInventory().drain(FluidHelper.copyStackWithAmount(fluidIngredient.stack(), fluidIngredient.amount()), IFluidHandler.FluidAction.SIMULATE);

        if (extractedFluid == FluidStack.EMPTY || extractedFluid.getAmount() < fluidIngredient.amount())
            return ItemStack.EMPTY;

        return JarPackageItem.slurp(getLevel(), getBlockPos(), Objects.requireNonNull(drainInventory.getInventory()), extractedFluid, ingredient.amount());
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

        BlockPos linkPos = ((PackagerIngredientBlockEntity) this).getLink();
        if (linkPos != null
                && level.getBlockEntity(linkPos) instanceof PackagerLinkBlockEntity plbe)
            ((LogisticallyLinkedIngredientBehaviour) plbe.behaviour).deductFromAccurateSummary(FluidUtil.getFluidContained(createdBox).orElse(FluidStack.EMPTY));

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

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (!clientPacket && compound.contains("LastSummary"))
            available = (IIngredientInventorySummary) InventorySummary.read(compound.getCompound("LastSummary"));
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if (clientPacket || available == null || available.isEmpty())
            return;
        compound.put("LastSummary", ((InventorySummary) available).write());
    }
}
