package ru.zznty.create_factory_logistics.compat.extra_gauges;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class AbstractPanelBehaviourStub {
    @Nullable
    private static Class<?> clazz;

    static {
        try {
            clazz = ModList.get().isLoaded("extra_gauges") ?
                    Class.forName("net.liukrast.eg.api.logistics.board.AbstractPanelBehaviour") : null;
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }

    public static boolean is(FactoryPanelBehaviour instance) {
        return clazz != null && clazz.isInstance(instance);
    }
}
