package ru.zznty.create_factory_logistics.compat.extra_gauges;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.liukrast.eg.api.logistics.board.AbstractPanelBehaviour;
import net.neoforged.fml.ModList;

public final class AbstractPanelBehaviourStub {
    private static final boolean isInstalled = ModList.get().isLoaded("extra_gauges");

    public static boolean shouldTick(FactoryPanelBehaviour instance) {
        if (!isInstalled) return false;

        return shouldTickInstalled(instance);
    }

    private static boolean shouldTickInstalled(FactoryPanelBehaviour instance) {
        if (instance instanceof AbstractPanelBehaviour behaviour) {
            return behaviour.skipOriginalTick();
        }
        return false;
    }
}
