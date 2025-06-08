package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;

import java.text.NumberFormat;
import java.text.ParseException;

public class FactoryFluidPanelScreen extends FactoryPanelScreen {
    private final FactoryFluidPanelBehaviour behaviour;
    private EditBox amountBox;

    public FactoryFluidPanelScreen(FactoryFluidPanelBehaviour behaviour) {
        super(behaviour);
        this.behaviour = behaviour;
    }

    @Override
    protected void init() {
        super.init();

        int x = guiLeft;
        int y = guiTop;

        amountBox = behaviour.panelBE().restocker ?
                    new EditBox(font, x + windowWidth - 80, y + 11, 60, 15, CommonComponents.EMPTY) :
                    new EditBox(font, x + windowWidth - 60, y + windowHeight - 90, 45, 15, CommonComponents.EMPTY);
        amountBox.setResponder(s -> {
            int value;
            try {
                value = parseFluidAmount(s);
                amountBox.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
            } catch (ParseException ignored) {
                amountBox.setTextColor(AbstractSimiWidget.COLOR_FAIL.getFirst().getRGB());
                return;
            }

            CatnipServices.NETWORK.sendToServer(new ValueSettingsPacket(behaviour.blockEntity.getBlockPos(), 0, value, null, null,
                    behaviour.blockEntity.getBlockState().getValue(FactoryFluidPanelBlock.FACING), false, behaviour.netId()));
        });
        amountBox.setTextColor(UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
        amountBox.setValue(FactoryFluidPanelBehaviour.formatLevel(behaviour.filter().amount(), false).string());
        addRenderableWidget(amountBox);
    }


    private static int parseFluidAmount(String s) throws ParseException {
        String mbStr = CreateLang.translate("generic.unit.millibuckets").string();
        String bStr = CreateLang.translate("generic.unit.buckets").string();
        NumberFormat format = LangNumberFormat.numberFormat.get();
        format.setParseIntegerOnly(true);

        s = s.trim();

        int value;
        if (s.endsWith(mbStr)) {
            value = format.parse(s.substring(0, s.length() - mbStr.length())).intValue();
        } else if (s.endsWith(bStr)) {
            value = format.parse(s.substring(0, s.length() - bStr.length())).intValue() * 1000;
        } else {
            value = format.parse(s).intValue();
        }

        if (value < 0) {
            throw new ParseException("Negative amount", 0);
        }
        return value;
    }
}
