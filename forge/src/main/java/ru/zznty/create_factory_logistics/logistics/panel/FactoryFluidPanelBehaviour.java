package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;

import java.util.List;

public class FactoryFluidPanelBehaviour extends FactoryPanelBehaviour implements GenericFilterProvider {
    public FactoryFluidPanelBehaviour(FactoryFluidPanelBlockEntity be, FactoryFluidPanelBlock.PanelSlot slot) {
        super(be, slot);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return FactoryPanelSetFluidMenu.create(containerId, playerInventory, this);
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        // i use original stack as filter because on 1.21 potions dont return their bottles as fluid bucket for some reason

        if (!stack.isEmpty() && !GenericItemEmptying.canItemBeEmptied(blockEntity.getLevel(), stack)) return false;

        return super.setFilter(stack);
    }

    public FluidStack getFluid() {
        return filter.fluid(blockEntity.getLevel());
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

        return CreateLang.number(Math.round((float) level / 100) / 10.).add(
                CreateLang.translate("generic.unit.buckets"));
    }

    public static LangBuilder formatLevel(int level, boolean round) {
        if (level >= BigItemStack.INF)
            return CreateLang.text("\u221e");

        if (!round || level < 1000 || level % 1000 != 0) {
            if (level % 1000 == 0)
                return CreateLang.number((float) level / 1000).add(CreateLang.translate("generic.unit.buckets"));

            return CreateLang.number(level).add(CreateLang.translate("generic.unit.millibuckets"));
        }

        // 1000 buckets
        if (level < 1_000_000) {
            float d = (float) level / 1000;
            if (d >= 100)
                d = Math.round(d);
            return CreateLang.number(d).add(CreateLang.translate("generic.unit.buckets"));
        }

        return CreateLang.number((float) level / 1_000_000)
                .add(CreateLang.text("k"))
                .add(CreateLang.translate("generic.unit.buckets"));
    }

    @Override
    public GenericStack filter() {
        // fluid panel doesnt use upTo field
        return FluidGenericStack.wrap(getFluid()).withAmount(count);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void displayScreen(Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new FactoryFluidPanelScreen(this));
    }
}
