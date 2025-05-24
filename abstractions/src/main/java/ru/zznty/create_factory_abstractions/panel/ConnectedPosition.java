package ru.zznty.create_factory_abstractions.panel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public interface ConnectedPosition {
    BlockPos position();

    record Single(BlockPos position) implements ConnectedPosition {
    }

    record GroupSlot(BlockPos position, int slot) implements ConnectedPosition {
    }

    static CompoundTag write(ConnectedPosition value) {
        CompoundTag tag = NbtUtils.writeBlockPos(value.position());
        if (value instanceof GroupSlot groupSlot) {
            tag.putInt("Slot", groupSlot.slot());
        }
        return tag;
    }

    static ConnectedPosition read(CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag);
        if (tag.contains("Slot")) {
            return new GroupSlot(pos, tag.getInt("Slot"));
        }
        return new Single(pos);
    }
}
