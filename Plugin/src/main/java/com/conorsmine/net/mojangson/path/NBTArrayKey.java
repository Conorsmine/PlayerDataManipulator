package com.conorsmine.net.mojangson.path;

public class NBTArrayKey extends NBTKey {

    private final int arrIndex;

    protected NBTArrayKey(String key) {
        super(key);

        this.arrIndex = Integer.parseInt(key.replaceAll("[\\[\\]]", ""));
    }

    public int getIndex() {
        return this.arrIndex;
    }

    public static NBTArrayKey parseToArrayKey(final int index) {
        if (index < 0) throw new NBTArrayKeyParseError();

        return new NBTArrayKey(String.format("[%d]", index));
    }

    public static class NBTArrayKeyParseError extends NBTKeyParseError { }
}
