package ru.zznty.create_factory_abstractions.panel;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GroupConnectionBehaviour extends ConnectedBehaviour {
    public static final BehaviourType<GroupConnectionBehaviour> TOP_LEFT = new BehaviourType<>("top_left");
    public static final BehaviourType<GroupConnectionBehaviour> TOP_RIGHT = new BehaviourType<>("top_right");
    public static final BehaviourType<GroupConnectionBehaviour> BOTTOM_LEFT = new BehaviourType<>("bottom_left");
    public static final BehaviourType<GroupConnectionBehaviour> BOTTOM_RIGHT = new BehaviourType<>("bottom_right");
    public static final List<BehaviourType<GroupConnectionBehaviour>> TYPES = List.of(
            TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    );

    public int slot;

    public GroupConnectionBehaviour(SmartBlockEntity be, int slot) {
        super(be);
        if (slot < 0 || slot > TYPES.size())
            throw new IllegalArgumentException("Slot must be between 0 and " + TYPES.size());
        this.slot = slot;
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        CompoundTag groupTag = nbt.getCompound(getType().getName());
        super.read(groupTag, clientPacket);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        CompoundTag groupTag = new CompoundTag();
        super.write(groupTag, clientPacket);
        nbt.put(getType().getName(), groupTag);
    }

    @Override
    protected @Nullable ConnectedBehaviour at(ConnectedPosition position) {
        if (position instanceof ConnectedPosition.GroupSlot groupSlot)
            return groupSlot.position().equals(blockEntity.getBlockPos()) && groupSlot.slot() == slot ? this : null;
        return super.at(position);
    }

    @Override
    public BehaviourType<GroupConnectionBehaviour> getType() {
        return TYPES.get(slot);
    }
}
