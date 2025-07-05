package ru.zznty.create_factory_logistics.logistics.abstractions.packager;

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_abstractions.generic.support.GenericIdentifiedInventory;

public abstract class AbstractPackagerBlockEntity extends PackagerBlockEntity {
    public final PackagerAttachedHandler handler;

    public AbstractPackagerBlockEntity(BlockEntityType<?> typeIn,
                                       BlockPos pos,
                                       BlockState state) {
        super(typeIn, pos, state);
        handler = createHandler();
    }

    protected abstract PackagerAttachedHandler createHandler();

    protected boolean supportsBlockEntity(BlockEntity target) {
        return target != null && !(target instanceof PortableStorageInterfaceBlockEntity);
    }

    @Override
    public boolean isTargetingSameInventory(@Nullable IdentifiedInventory inventory) {
        if (inventory == null)
            return false;

        GenericIdentifiedInventory inv = GenericIdentifiedInventory.from(inventory);
        return targetsSameInventory(inv);
    }

    protected abstract boolean targetsSameInventory(GenericIdentifiedInventory inv);
}
