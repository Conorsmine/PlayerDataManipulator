package com.conorsmine.net.mojangson.path;

import com.conorsmine.net.mojangson.MojangsonUtils;
import org.jetbrains.annotations.NotNull;

public class NBTArrayKey extends NBTKey {

    private final int arrIndex;

    protected NBTArrayKey(MojangsonUtils utils, String key) {
        super(utils, key);

        this.arrIndex = Integer.parseInt(key.replaceAll("[\\[]]", ""));
    }

    public int getIndex() {
        return this.arrIndex;
    }

    public static NBTArrayKey parseToArrayKey(@NotNull final MojangsonUtils utils, final int index) {
        if (index < 0) throw new NBTArrayKeyParseError();

        return new NBTArrayKey(utils, String.valueOf(index));
    }

    static class NBTArrayKeyParseError extends NBTKeyParseError { }
}
