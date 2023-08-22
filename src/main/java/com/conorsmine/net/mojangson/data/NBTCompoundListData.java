package com.conorsmine.net.mojangson.data;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTListCompound;

import java.util.List;

public class NBTCompoundListData implements INBTListData<NBTListCompound>, ICompoundData<List<NBTListCompound>> {

    private final NBTCompoundList nbtCompoundList;

    public NBTCompoundListData(NBTCompoundList nbtCompoundList) {
        this.nbtCompoundList = nbtCompoundList;
    }

    @Override
    public NBTDataType getType() {
        return NBTDataType.COMPOUND_LIST;
    }

    @Override
    public NBTCompoundList getData() {
        return nbtCompoundList;
    }
}
