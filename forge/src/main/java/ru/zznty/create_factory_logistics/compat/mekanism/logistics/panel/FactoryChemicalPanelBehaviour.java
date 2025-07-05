package ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.utility.CreateLang;
import com.tterrag.registrate.util.entry.BlockEntry;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.ChemicalUtil;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableObject;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlocks;
import ru.zznty.create_factory_logistics.compat.mekanism.generic.ChemicalGenericStack;
import ru.zznty.create_factory_logistics.logistics.abstractions.panel.AbstractFactoryPanelBehaviour;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBlock;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelScreen;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryPanelSetFluidMenu;
import ru.zznty.create_factory_logistics.logistics.panel.PanelModelProvider;

import java.util.List;

import static ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour.formatLevel;

public class FactoryChemicalPanelBehaviour extends AbstractFactoryPanelBehaviour implements GenericFilterProvider, PanelModelProvider {
    public FactoryChemicalPanelBehaviour(FactoryChemicalPanelBlockEntity be, FactoryFluidPanelBlock.PanelSlot slot) {
        super(be, slot);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return FactoryPanelSetFluidMenu.create(containerId, playerInventory, this);
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        if (stack.isEmpty()) return super.setFilter(stack);

        if (getChemicalStack(stack).isEmpty())
            return false;

        return super.setFilter(stack);
    }

    public Chemical<?> getChemical() {
        if (filter.item().isEmpty())
            return MekanismAPI.EMPTY_GAS;
        return getChemicalStack(filter.item()).getRaw();
    }

    public static ChemicalStack<?> getChemicalStack(ItemStack stack) {
        if (stack.isEmpty())
            return MekanismAPI.EMPTY_GAS.getStack(0);

        MutableObject<ChemicalStack<?>> chemical = new MutableObject<>(MekanismAPI.EMPTY_GAS.getStack(0));
        if (chemical.getValue().isEmpty() && ChemicalUtil.hasGas(stack))
            stack.getCapability(Capabilities.GAS_HANDLER)
                    .ifPresent(cap -> chemical.setValue(getChemical(cap)));
        if (chemical.getValue().isEmpty() && ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(),
                                                                      Capabilities.INFUSION_HANDLER))
            stack.getCapability(Capabilities.INFUSION_HANDLER)
                    .ifPresent(cap -> chemical.setValue(getChemical(cap)));
        if (chemical.getValue().isEmpty() && ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(),
                                                                      Capabilities.PIGMENT_HANDLER))
            stack.getCapability(Capabilities.PIGMENT_HANDLER)
                    .ifPresent(cap -> chemical.setValue(getChemical(cap)));
        if (chemical.getValue().isEmpty() && ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(),
                                                                      Capabilities.SLURRY_HANDLER))
            stack.getCapability(Capabilities.SLURRY_HANDLER)
                    .ifPresent(cap -> chemical.setValue(getChemical(cap)));

        return chemical.getValue();
    }

    private static ChemicalStack<?> getChemical(IChemicalHandler<?, ?> handler) {
        if (handler.getTanks() < 1) return MekanismAPI.EMPTY_GAS.getStack(0);
        return handler.getChemicalInTank(0);
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

    @Override
    public GenericStack filter() {
        // chemical panel doesnt use upTo field
        return ChemicalGenericStack.wrap(getChemical().getStack(count));
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void displayScreen(Player player) {
        if (player instanceof LocalPlayer)
            ScreenOpener.open(new FactoryFluidPanelScreen(this));
    }

    @Override
    public BlockEntry<? extends FactoryPanelBlock> model() {
        return FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE;
    }

    @Override
    public ItemStack defaultPackage() {
        return ItemStack.EMPTY;
    }
}
