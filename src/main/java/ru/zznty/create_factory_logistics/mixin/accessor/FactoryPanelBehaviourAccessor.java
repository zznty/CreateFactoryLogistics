package ru.zznty.create_factory_logistics.mixin.accessor;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FactoryPanelBehaviour.class)
public interface FactoryPanelBehaviourAccessor {
    @Invoker(remap = false)
    InventorySummary callGetRelevantSummary();
}
