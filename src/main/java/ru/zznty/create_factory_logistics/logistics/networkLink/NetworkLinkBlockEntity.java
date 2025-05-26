package ru.zznty.create_factory_logistics.logistics.networkLink;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyRegistration;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;

import java.util.List;

public class NetworkLinkBlockEntity extends SmartBlockEntity {
    private LogisticallyLinkedBehaviour link;
    @Nullable
    private GenericKeyRegistration registration;
    private ScrollOptionBehaviour<NetworkLinkMode> scroll;

    public NetworkLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(link = new LogisticallyLinkedBehaviour(this, false));
        scroll = new ScrollOptionBehaviour<>(NetworkLinkMode.class,
                                             Component.translatable("create_factory_logistics.gui.network_link.mode"),
                                             this, new ValueBox());

        behaviours.add(scroll);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains(NetworkLinkBlock.INGREDIENT_TYPE, CompoundTag.TAG_STRING))
            registration = GenericContentExtender.REGISTRY.get(ResourceLocation.parse(tag.getString(NetworkLinkBlock.INGREDIENT_TYPE)));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (registration != null)
            tag.putString(NetworkLinkBlock.INGREDIENT_TYPE,
                          GenericContentExtender.REGISTRY.get().getKey(registration).toString());
    }

    public NetworkLinkMode mode() {
        return scroll.get();
    }

    public GenericKeyRegistration provider() {
        return registration;
    }

    private static class ValueBox extends ValueBoxTransform.Sided {
        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            float yRot = AngleHelper.horizontalAngle(state.getValue(NetworkLinkBlock.FACE) == AttachFace.FLOOR ?
                                                     state.getValue(NetworkLinkBlock.FACING) :
                                                     getSide().getOpposite());
            float xRot = AngleHelper.verticalAngle(getSide().getOpposite());
            TransformStack.of(ms)
                    .rotateYDegrees(yRot)
                    .rotateXDegrees(xRot);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Vec3 location;
            if (state.getValue(NetworkLinkBlock.FACE) == AttachFace.FLOOR) {
                location = VecHelper.voxelSpace(8, 6, 5);
                location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(
                        state.getValue(NetworkLinkBlock.FACING)), Direction.Axis.Y);
            } else {
                location = VecHelper.voxelSpace(8, 5, 6);
                location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
            }
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            return location;
        }

        @Override
        protected Vec3 getSouthLocation() {
            return null;
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            if (state.getValue(NetworkLinkBlock.FACE) == AttachFace.FLOOR)
                return direction == Direction.UP;
            return state.getValue(NetworkLinkBlock.FACING) == direction;
        }
    }
}
