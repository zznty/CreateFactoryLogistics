package ru.zznty.create_factory_logistics.logistics.abstractions.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;

public abstract class AbstractFactoryPanelBehaviour extends FactoryPanelBehaviour {
    public AbstractFactoryPanelBehaviour(FactoryPanelBlockEntity be,
                                         FactoryPanelBlock.PanelSlot slot) {
        super(be, slot);
    }
}
