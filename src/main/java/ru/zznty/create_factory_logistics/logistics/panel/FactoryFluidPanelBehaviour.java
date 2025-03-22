package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import ru.zznty.create_factory_logistics.logistics.panel.request.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.panel.request.IngredientPromiseQueue;
import ru.zznty.create_factory_logistics.logistics.stock.IFluidInventorySummary;
import ru.zznty.create_factory_logistics.mixin.accessor.FactoryPanelBehaviourAccessor;

import java.util.List;

public class FactoryFluidPanelBehaviour extends FactoryPanelBehaviour {
    public FactoryFluidPanelBehaviour(FactoryFluidPanelBlockEntity be, FactoryFluidPanelBlock.PanelSlot slot) {
        super(be, slot);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return FactoryPanelSetFluidMenu.create(containerId, playerInventory, this);
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        if (!GenericItemEmptying.canItemBeEmptied(blockEntity.getLevel(), stack))
            return false;

        Pair<FluidStack, ItemStack> emptyResult = GenericItemEmptying.emptyItem(blockEntity.getLevel(), stack, true);

        if (emptyResult.getFirst().isEmpty())
            return false;

        Item bucket = emptyResult.getFirst().getFluid().getBucket();

        this.filter = FilterItemStack.of(bucket == Items.AIR ? stack : new ItemStack(bucket));
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    public FluidStack getFluid() {
        return filter.fluid(blockEntity.getLevel());
    }

    @Override
    public int getLevelInStorage() {
        if (blockEntity.isVirtual())
            return 1;
        if (getWorld().isClientSide())
            return super.getLevelInStorage();
        if (getFilter().isEmpty())
            return 0;

        IFluidInventorySummary summary = getRelevantSummary();
        return summary.getCountOf(filter.fluid(blockEntity.getLevel()).getFluid());
    }

    @Override
    public int getPromised() {
        if (getWorld().isClientSide())
            return super.getPromised();
        FluidStack fluid = getFluid();
        if (fluid.isEmpty())
            return 0;

        BoardIngredient ingredient = BoardIngredient.of(this);

        if (panelBE().restocker) {
            IngredientPromiseQueue restockerPromiseQueue = (IngredientPromiseQueue) restockerPromises;

            if (forceClearPromises) {

                restockerPromiseQueue.forceClear(ingredient);

                resetTimerSlightly();
            }
            forceClearPromises = false;
            return restockerPromiseQueue.getTotalPromisedAndRemoveExpired(ingredient, getPromiseExpiryTimeInTicks());
        }

        IngredientPromiseQueue promises = (IngredientPromiseQueue) Create.LOGISTICS.getQueuedPromises(network);
        if (promises == null)
            return 0;

        if (forceClearPromises) {
            promises.forceClear(ingredient);
            resetTimerSlightly();
        }
        forceClearPromises = false;

        return promises.getTotalPromisedAndRemoveExpired(ingredient, getPromiseExpiryTimeInTicks());
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (settings.row() == 1)
            settings = new ValueSettings(settings.row(), settings.value() * 1000);
        super.setValueSettings(player, settings, ctrlDown);
    }

    @Override
    public MutableComponent getCountLabelForValueBox() {
        if (filter.isEmpty())
            return Component.empty();
        if (waitingForNetwork) {
            return Component.literal("?");
        }

        int levelInStorage = getLevelInStorage();
        int promised = getPromised();

        if (count == 0) {
            return formatLevel(levelInStorage)
                    .color(0xF1EFE8)
                    .component();
        }

        return formatLevel(levelInStorage)
                .color(satisfied ? 0xD7FFA8 : promisedSatisfied ? 0xffcd75 : 0xFFBFA8)
                .add(CreateLang.text(promised == 0 ? "" : "\u23F6"))
                .add(CreateLang.text("/")
                        .style(ChatFormatting.WHITE))
                .add(formatLevel(count)
                        .color(0xF1EFE8))
                .component();
    }

    @Override
    public MutableComponent getLabel() {
        String key = "";

        if (!targetedBy.isEmpty() && count == 0)
            return CreateLang.translate("gui.factory_panel.no_target_amount_set")
                    .style(ChatFormatting.RED)
                    .component();

        if (isMissingAddress())
            return CreateLang.translate("gui.factory_panel.address_missing")
                    .style(ChatFormatting.RED)
                    .component();

        if (getFilter().isEmpty())
            key = "factory_panel.new_factory_task";
        else if (waitingForNetwork)
            key = "factory_panel.some_links_unloaded";
        else if (getAmount() == 0 || targetedBy.isEmpty())
            return getFluid().getDisplayName()
                    .plainCopy();
        else {
            key = getFilter().getDisplayName()
                    .getString();
            if (redstonePowered)
                key += " " + CreateLang.translate("factory_panel.redstone_paused")
                        .string();
            else if (!satisfied)
                key += " " + CreateLang.translate("factory_panel.in_progress")
                        .string();
            return CreateLang.text(key)
                    .component();
        }

        return CreateLang.translate(key)
                .component();
    }

    @Override
    public MutableComponent formatValue(ValueSettings value) {
        if (value.value() == 0) {
            return CreateLang.translateDirect("gui.factory_panel.inactive");
        } else {
            return formatLevel(value.row() == 0 ? value.value() : value.value() * 1000).component();
        }
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        int maxAmount = 100;
        return new ValueSettingsBoard(CreateLang.translate("factory_panel.target_amount")
                .component(), maxAmount, 10,
                List.of(CreateLang.translate("schedule.condition.threshold.buckets")
                                .component(),
                        CreateLang.translate("schedule.condition.threshold.buckets")
                                .component()),
                new ValueSettingsFormatter(this::formatValue));
    }

    public static LangBuilder formatLevel(int level) {
        return formatLevel(level, true);
    }

    public static LangBuilder formatLevelShort(int level) {
        if (level >= BigItemStack.INF)
            return CreateLang.text("\u221e");

        if (level < 100)
            return CreateLang.number(level).add(CreateLang.translate("generic.unit.millibuckets"));

        return CreateLang.number(Math.round((float) level / 100) / 10.).add(CreateLang.translate("generic.unit.buckets"));
    }

    public static LangBuilder formatLevel(int level, boolean round) {
        if (level >= BigItemStack.INF)
            return CreateLang.text("\u221e");

        if (!round || level < 1000 || level % 1000 != 0) {
            return CreateLang.number(level).add(CreateLang.translate("generic.unit.millibuckets"));
        }

        if (level < 100_001) {
            return CreateLang.number(level / 1000).add(CreateLang.translate("generic.unit.buckets"));
        }

        return CreateLang.number(level / 1_000_000)
                .add(CreateLang.text("k"))
                .add(CreateLang.translate("generic.unit.buckets"));
    }

    private IFluidInventorySummary getRelevantSummary() {
        return (IFluidInventorySummary) ((FactoryPanelBehaviourAccessor) this).callGetRelevantSummary();
    }

    private int getPromiseExpiryTimeInTicks() {
        if (promiseClearingInterval == -1)
            return -1;
        if (promiseClearingInterval == 0)
            return 20 * 30;

        return promiseClearingInterval * 20 * 60;
    }
}
