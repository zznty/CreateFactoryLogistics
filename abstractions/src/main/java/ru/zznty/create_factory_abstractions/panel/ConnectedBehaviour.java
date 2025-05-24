package ru.zznty.create_factory_abstractions.panel;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectedBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<ConnectedBehaviour> TYPE = new BehaviourType<>();

    public Set<ConnectedPosition> targeting = new HashSet<>();
    public Map<ConnectedPosition, Connection> targetedBy = new HashMap<>();

    public ConnectedBehaviour(SmartBlockEntity be) {
        super(be);
    }

    public void connect(ConnectedBehaviour behaviour) {
        if (behaviour == this)
            return;
        behaviour.targeting.add(position());
        targetedBy.put(behaviour.position(), new Connection.Simple(behaviour.position()));
        behaviour.blockEntity.notifyUpdate();
    }

    public void disconnect(ConnectedBehaviour behaviour) {
        if (behaviour == this)
            return;
        behaviour.targeting.remove(position());
        targetedBy.remove(behaviour.position());
        behaviour.blockEntity.notifyUpdate();
    }

    public void moveTo(ConnectedPosition newPosition, ServerPlayer player) {
        Level level = getWorld();
        BlockState existingState = level.getBlockState(newPosition.position());

        // Check if target pos is valid
        if (ConnectedBehaviour.at(level, newPosition) != null)
            return;
        boolean isAddedToOtherGauge = blockEntity.getBlockState().is(existingState.getBlock());
        if (!existingState.isAir() && !isAddedToOtherGauge)
            return;
        if (isAddedToOtherGauge && existingState != blockEntity.getBlockState())
            return;
        if (!isAddedToOtherGauge)
            level.setBlock(newPosition.position(), blockEntity.getBlockState(), 3);

        for (ConnectedPosition blockPos : targetedBy.keySet())
            if (!blockPos.position().closerThan(newPosition.position(), 24))
                return;
        for (ConnectedPosition blockPos : targeting)
            if (!blockPos.position().closerThan(newPosition.position(), 24))
                return;

        SmartBlockEntity oldBlockEntity = blockEntity;
        ConnectedPosition oldPosition = position();

        moveSuccess(newPosition, player);

        // Add to new BE
        if (level.getBlockEntity(newPosition.position()) instanceof SmartBlockEntity be) {
            be.attachBehaviourLate(this);
            be.notifyUpdate();
        }

        // Remove from old BE
        oldBlockEntity.removeBehaviour(getType());
        oldBlockEntity.notifyUpdate();

        // Rewire connections
        for (ConnectedPosition position : targeting) {
            @Nullable ConnectedBehaviour at = at(level, position);
            if (at != null) {
                // todo copy connection data somehow
                Connection connection = at.targetedBy.remove(oldPosition);
                at.targetedBy.put(newPosition, new Connection.Simple(newPosition));
                at.blockEntity.sendData();
            }
        }

        for (ConnectedPosition position : targetedBy.keySet()) {
            @Nullable ConnectedBehaviour at = at(level, position);
            if (at != null) {
                at.targeting.remove(oldPosition);
                at.targeting.add(newPosition);
            }
        }
    }

    protected void moveSuccess(ConnectedPosition newPosition, ServerPlayer player) {
        // Tell player
        player.displayClientMessage(CreateLang.translate("factory_panel.relocated")
                                            .style(ChatFormatting.GREEN)
                                            .component(), true);
        player.level()
                .playSound(null, newPosition.position(), SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void destroy() {
        ConnectedPosition position = position();
        for (ConnectedPosition target : targeting) {
            ConnectedBehaviour behaviour = at(getWorld(), target);
            if (behaviour != null) {
                behaviour.targetedBy.remove(position);
                behaviour.blockEntity.sendData();
            }
        }
        for (ConnectedPosition target : targetedBy.keySet()) {
            ConnectedBehaviour behaviour = at(getWorld(), target);
            if (behaviour != null) {
                behaviour.targeting.remove(position);
                behaviour.blockEntity.sendData();
            }
        }
        super.destroy();
    }

    protected ConnectedPosition position() {
        return new ConnectedPosition.Single(getPos());
    }

    @Nullable
    protected ConnectedBehaviour at(ConnectedPosition position) {
        if (position.position().equals(blockEntity.getBlockPos()))
            return this;
        return null;
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        targeting.addAll(NBTHelper.readCompoundList(nbt.getList("Targeting", CompoundTag.TAG_COMPOUND),
                                                    ConnectedPosition::read));
        NBTHelper.iterateCompoundList(nbt.getList("TargetedBy", CompoundTag.TAG_COMPOUND), tag -> {
            Connection connection = Connection.read(tag);
            targetedBy.put(connection.from(), connection);
        });
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.put("Targeting", NBTHelper.writeCompoundList(targeting, ConnectedPosition::write));
        nbt.put("TargetedBy", NBTHelper.writeCompoundList(targetedBy.values(), Connection::write));
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Nullable
    public static ConnectedBehaviour at(BlockGetter world, ConnectedPosition position) {
        ConnectedBehaviour behaviour = get(world, position.position(), TYPE);
        return behaviour == null ? null : behaviour.at(position);
    }
}
