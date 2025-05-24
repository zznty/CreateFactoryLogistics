package ru.zznty.create_factory_abstractions.panel;

import net.minecraft.nbt.CompoundTag;

public interface Connection {
    ConnectedPosition from();

    record Simple(ConnectedPosition from) implements Connection {
    }

    static CompoundTag write(Connection connection) {
        return ConnectedPosition.write(connection.from());
    }

    static Connection read(CompoundTag nbt) {
        return new Simple(ConnectedPosition.read(nbt));
    }
}
