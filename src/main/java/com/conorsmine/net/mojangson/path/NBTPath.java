package com.conorsmine.net.mojangson.path;

import com.conorsmine.net.mojangson.MojangsonUtils;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NBTPath {

    private int index = 0;
    private final List<NBTKey> keyList = new LinkedList<>();
    private final MojangsonUtils utils;

    protected NBTPath(MojangsonUtils utils) {
        this.utils = utils;
    }

    protected void addKey(final NBTKey key) {
        this.keyList.add(key);
    }

    @Nullable
    public NBTKey next() {
        if (this.index >= this.keyList.size()) return null;
        return this.keyList.get(index++);
    }

    @Nullable
    public NBTKey previous() {
        if (this.index <= 1 || this.keyList.size() == 0) return null;
        return this.keyList.get(--index);
    }

    public boolean isEmpty() {
        return this.keyList.isEmpty();
    }

    public boolean isLastKey() {
        return (this.index == keyList.size());
    }

    public void toBeginning() {
        this.index = 0;
    }

    @Nullable
    public NBTKey getFirstKey() {
        if (isEmpty()) return null;
        return this.keyList.get(0);
    }

    public void toEnd() {
        this.index = this.keyList.size() - 1;
    }

    @Nullable
    public NBTKey getLastKey() {
        if (isEmpty()) return null;
        return this.keyList.get(this.keyList.size() - 1);
    }

    @Override
    public String toString() {
        return this.keyList.stream().map(NBTKey::getKeyValue).collect(Collectors.joining(this.utils.getSeparator()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NBTPath nbtPath = (NBTPath) o;
        return this.keyList.equals(nbtPath.keyList);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
