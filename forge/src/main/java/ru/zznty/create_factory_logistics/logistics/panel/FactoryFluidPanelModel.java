package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.resources.model.BakedModel;
import ru.zznty.create_factory_logistics.FactoryModels;
import ru.zznty.create_factory_abstractions.logistics.panel.AbstractPanelModel;

public class FactoryFluidPanelModel extends AbstractPanelModel {
    public FactoryFluidPanelModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    protected PartialModel getModel(FactoryPanelBlock.PanelType type, FactoryPanelBlock.PanelState panelState) {
        return panelState == FactoryPanelBlock.PanelState.PASSIVE
                ? type == FactoryPanelBlock.PanelType.NETWORK ? FactoryModels.FACTORY_FLUID_PANEL : FactoryModels.FACTORY_FLUID_PANEL_RESTOCKER
                : type == FactoryPanelBlock.PanelType.NETWORK ? FactoryModels.FACTORY_FLUID_PANEL_WITH_BULB
                  : FactoryModels.FACTORY_FLUID_PANEL_RESTOCKER_WITH_BULB;
    }
}
