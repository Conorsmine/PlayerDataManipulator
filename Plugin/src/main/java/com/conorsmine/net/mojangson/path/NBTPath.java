package com.conorsmine.net.mojangson.path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NBTPath implements Iterable<NBTKey> {
    private final List<NBTKey> keyList = new LinkedList<>();
    private final String sep;

    protected NBTPath(String sep) {
        this.sep = sep;
    }

    protected void addKey(final NBTKey key) {
        this.keyList.add(key);
    }

    public int size() {
        return this.keyList.size();
    }

    @Override
    public Iterator<NBTKey> iterator() {
        return new Iterator<NBTKey>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return size() > index;
            }

            @Override
            @Nullable
            public NBTKey next() {
                if (this.index >= size()) return null;
                return keyList.get(index++);
            }
        };
    }

    public NBTKey get(int i) {
        if (i < 0) i = size() + i;
        if (i < 0 || i >= size()) throw new NullPointerException("Index may not be negative, larger or equals the size of the key list!");

        return this.keyList.get(i);
    }

    public NBTKey getFirstKey() {
        return get(0);
    }

    public NBTKey getLastKey() {
        return get(-1);
    }

    public boolean isEmpty() {
        return this.keyList.isEmpty();
    }

    public boolean startsWith(@NotNull final NBTPath otherPath) {
        if (otherPath.size() > size()) return false;
        return getSubSet(0, otherPath.size()).equals(otherPath);
    }

    public boolean endsWith(@NotNull final NBTPath otherPath) {
        if (otherPath.size() > size()) return false;
        return getSubSet(size() - otherPath.size(), size()).equals(otherPath);
    }

    public boolean contains(@NotNull final NBTPath otherPath) {
        if (otherPath.size() > size()) return false;
        for (int i = 0; i < (size() - otherPath.size() + 1); i++) {
            if (getSubSet(i, i + otherPath.size()).equals(otherPath)) return true;
        }

        return false;
    }

    public NBTPath getSubSet(int startIndex, int endIndex) {
        if (startIndex < 0) startIndex = size() + startIndex;
        if (endIndex < 0) endIndex = size() + endIndex;

        if (startIndex < 0 || startIndex > size()) throw new NullPointerException("startIndex may not be negative, larger or equals the size of the key list!");
        if (endIndex < 0 || endIndex > size()) throw new NullPointerException("endIndex may not be negative, larger or equals the size of the key list!");

        final NBTPathBuilder builder = new NBTPathBuilder(this.sep);
        this.keyList.subList(startIndex, endIndex).forEach(builder::addNBTKey);
        return builder.create();
    }

    @Override
    public String toString() {
        return this.keyList.stream().map(NBTKey::getKeyValue).collect(Collectors.joining(this.sep));
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
