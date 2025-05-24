package ru.zznty.create_factory_abstractions.panel;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import org.jetbrains.annotations.Nullable;

public class GroupConnectionRootBehaviour extends ConnectedBehaviour {
    public GroupConnectionRootBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    protected @Nullable ConnectedBehaviour at(ConnectedPosition position) {
        if (position instanceof ConnectedPosition.GroupSlot groupSlot && groupSlot.position().equals(getPos())) {
            GroupConnectionBehaviour behaviour = blockEntity.getBehaviour(
                    GroupConnectionBehaviour.TYPES.get(groupSlot.slot()));
            return behaviour == null ? null : behaviour.at(position);
        }
        return super.at(position);
    }
}
