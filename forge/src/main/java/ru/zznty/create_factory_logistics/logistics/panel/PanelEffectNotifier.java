package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;

@FunctionalInterface
public interface PanelEffectNotifier {
    void send(FactoryPanelPosition from, FactoryPanelPosition to, boolean success);
}
