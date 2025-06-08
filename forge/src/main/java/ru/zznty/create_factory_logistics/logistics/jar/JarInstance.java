package ru.zznty.create_factory_logistics.logistics.jar;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;

public class JarInstance extends TransformedInstance {
    public JarInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
        super(type, handle);
    }
}
