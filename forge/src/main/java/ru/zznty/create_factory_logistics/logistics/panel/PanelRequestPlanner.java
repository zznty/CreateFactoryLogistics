package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import net.minecraft.world.level.Level;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericInventorySummary;
import ru.zznty.create_factory_abstractions.generic.support.PanelRequestedStacks;
import ru.zznty.create_factory_logistics.config.WorldConfig;

import java.util.List;
import java.util.Set;

public final class PanelRequestPlanner {
    private final FactoryPanelBehaviour panel;
    private final PanelEffectNotifier effectSender;

    public PanelRequestPlanner(FactoryPanelBehaviour panel,
                               PanelEffectNotifier effectSender) {
        this.panel = panel;
        this.effectSender = effectSender;
    }

    public boolean requestDependent(List<PanelRequestedStacks> toRequest,
                                    FactoryPanelConnection sourceConnection,
                                    FactoryPanelBehaviour context,
                                    Set<FactoryPanelPosition> visited) {
        FactoryPanelBehaviour source = FactoryPanelBehaviour.at(panel.getWorld(), sourceConnection);
        if (source == null)
            return false;

        if (!visited.add(sourceConnection.from)) {
            return source.satisfied || source.promisedSatisfied;
        }

        GenericStack ingredient = GenericStack.of(source).withAmount(sourceConnection.amount);
        GenericInventorySummary summary = GenericInventorySummary.of(
                LogisticsManager.getSummaryOfNetwork(source.network, true));

        boolean canCascade = WorldConfig.factoryGaugeCascadeRequest
                && !source.targetedBy.isEmpty() && !source.recipeAddress.isBlank();

        if (ingredient.isEmpty() || (summary.isEmpty() && !canCascade)) {
            effectSender.send(sourceConnection.from, context.getPanelPosition(), false);
            return false;
        }

        if (!summary.isEmpty() && summary.getCountOf(ingredient.key()) >= ingredient.amount())
            return true;

        if (source.getLevelInStorage() + source.getPromised() >= ingredient.amount()) {
            return false;
        }

        if (canCascade) {
            for (FactoryPanelConnection connection : source.targetedBy.values()) {
                if (!requestDependent(toRequest, connection, source, visited)) {
                    // A cascaded dependency of this gauge couldn't be fulfilled. Blink this gauge's incoming
                    // path red too, so the failure keeps propagating up the chain instead of the red path
                    // stopping at the deepest failing gauge (e.g. planks) and never reaching this one.
                    effectSender.send(sourceConnection.from, context.getPanelPosition(), false);
                    return false;
                }
            }
            for (FactoryPanelConnection connection : source.targetedBy.values()) {
                visited.remove(connection.from);
            }
        } else {
            effectSender.send(sourceConnection.from, context.getPanelPosition(), false);
            return false;
        }

        toRequest.add(PanelRequestedStacks.of(source));

        effectSender.send(sourceConnection.from, context.getPanelPosition(), true);
        return true;
    }
}
