package ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.resources.model.BakedModel;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismModels;
import ru.zznty.create_factory_logistics.logistics.abstractions.panel.AbstractPanelModel;

public class FactoryChemicalPanelModel extends AbstractPanelModel {
    public FactoryChemicalPanelModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    protected PartialModel getModel(FactoryPanelBlock.PanelType type, FactoryPanelBlock.PanelState panelState) {
        return panelState == FactoryPanelBlock.PanelState.PASSIVE
               ?
               type == FactoryPanelBlock.PanelType.NETWORK ?
               FactoryMekanismModels.FACTORY_CHEMICAL_PANEL :
               FactoryMekanismModels.FACTORY_CHEMICAL_PANEL_RESTOCKER
               :
               type == FactoryPanelBlock.PanelType.NETWORK ?
               FactoryMekanismModels.FACTORY_CHEMICAL_PANEL_WITH_BULB
                                                           :
               FactoryMekanismModels.FACTORY_CHEMICAL_PANEL_RESTOCKER_WITH_BULB;
    }
}
