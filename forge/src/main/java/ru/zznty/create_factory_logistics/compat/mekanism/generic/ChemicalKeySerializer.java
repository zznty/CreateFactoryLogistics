package ru.zznty.create_factory_logistics.compat.mekanism.generic;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeySerializer;

public class ChemicalKeySerializer implements GenericKeySerializer<ChemicalKey> {
    @Override
    public ChemicalKey read(CompoundTag tag) {
        ChemicalType chemicalType = ChemicalType.fromNBT(tag);
        if (chemicalType == null) return new ChemicalKey(MekanismAPI.EMPTY_GAS);

        Chemical<?> chemical = switch (chemicalType) {
            case GAS -> Gas.readFromNBT(tag);
            case INFUSION -> InfuseType.readFromNBT(tag);
            case PIGMENT -> Pigment.readFromNBT(tag);
            case SLURRY -> Slurry.readFromNBT(tag);
        };

        return new ChemicalKey(chemical);
    }

    @Override
    public void write(ChemicalKey key, CompoundTag tag) {
        Chemical<?> chemical = key.chemical();
        ChemicalType.getTypeFor(chemical).write(tag);
        chemical.write(tag);
    }

    @Override
    public ChemicalKey read(FriendlyByteBuf buf) {
        return read(buf.readNbt());
    }

    @Override
    public void write(ChemicalKey key, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        write(key, tag);
        buf.writeNbt(tag);
    }
}
