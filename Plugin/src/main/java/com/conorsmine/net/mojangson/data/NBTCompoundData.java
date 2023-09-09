package com.conorsmine.net.mojangson.data;

import de.tr7zw.nbtapi.NBTCompound;

public class NBTCompoundData implements ICompoundData<NBTCompound> {

    private final NBTCompound nbt;

    public NBTCompoundData(NBTCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public NBTDataType getType() {
        return NBTDataType.COMPOUND;
    }

    @Override
    public NBTCompound getData() {
        return nbt;
    }
}
