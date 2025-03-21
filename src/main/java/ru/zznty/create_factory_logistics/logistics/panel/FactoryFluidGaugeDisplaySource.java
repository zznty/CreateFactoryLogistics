package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.ValueListDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.createmod.catnip.data.IntAttached;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FactoryFluidGaugeDisplaySource extends ValueListDisplaySource {
    @Override
    protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
        List<FactoryPanelPosition> panels = context.blockEntity().factoryPanelSupport.getLinkedPanels();
        if (panels.isEmpty())
            return Stream.empty();
        return panels.stream()
                .map(fpp -> createEntry(context.level(), fpp))
                .filter(Objects::nonNull)
                .limit(maxRows);
    }

    @Nullable
    public IntAttached<MutableComponent> createEntry(Level level, FactoryPanelPosition pos) {
        FactoryPanelBehaviour panel = FactoryPanelBehaviour.at(level, pos);
        if (!(panel instanceof FactoryFluidPanelBehaviour fluidPanel))
            return null;

        FluidStack fluid = fluidPanel.getFluid();

        int demand = panel.getAmount();
        String s = " ";

        if (demand != 0) {
            int promised = panel.getPromised();
            if (panel.satisfied)
                s = "\u2714";
            else if (promised != 0)
                s = "\u2191";
            else
                s = "\u25aa";
        }

        return IntAttached.with(panel.getLevelInStorage(), Component.literal(s + " ")
                .withStyle(style -> style.withColor(panel.getIngredientStatusColor()))
                .append(fluid.getDisplayName()
                        .plainCopy()
                        .withStyle(ChatFormatting.RESET)));
    }

    @Override
    protected List<MutableComponent> createComponentsFromEntry(DisplayLinkContext context,
                                                               IntAttached<MutableComponent> entry) {
        return List.of();
    }

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        List<MutableComponent> result = new ArrayList<>();
        for (Iterator<IntAttached<MutableComponent>> it = provideEntries(context, stats.maxRows()).iterator(); it.hasNext(); ) {
            IntAttached<MutableComponent> entry = it.next();

            result.add((shortenNumbers(context) ? FactoryFluidPanelBehaviour.formatLevelShort(entry.getFirst()) : FactoryFluidPanelBehaviour.formatLevel(entry.getFirst()))
                    .space()
                    .add(entry.getValue())
                    .component());
        }

        return result;
    }

    @Override
    public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        List<List<MutableComponent>> result = new ArrayList<>();
        for (Iterator<IntAttached<MutableComponent>> it = provideEntries(context, stats.maxRows()).iterator(); it.hasNext(); ) {
            IntAttached<MutableComponent> entry = it.next();

            result.add(List.of((shortenNumbers(context) ? FactoryFluidPanelBehaviour.formatLevelShort(entry.getFirst()) : FactoryFluidPanelBehaviour.formatLevel(entry.getFirst()))
                    .space()
                    .add(entry.getValue())
                    .component()));
        }

        return result;
    }

    // todo layout cringe

    @Override
    protected String getTranslationKey() {
        return "gauge_status";
    }

    @Override
    protected boolean valueFirst() {
        return true;
    }
}
