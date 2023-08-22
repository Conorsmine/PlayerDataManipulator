package com.conorsmine.net;

import com.conorsmine.net.mojangson.path.NBTPath;

public class InventoryPath {

    private final String sectionName;
    private final NBTPath path;
    private final int size;

    public InventoryPath(NBTPath path, String sectionName, int size) {
        this.path = path;
        this.sectionName = sectionName;
        this.size = size;
    }

    public NBTPath getPath() {
        return path;
    }

    public String getSectionName() {
        return sectionName;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "PathWrapper{" +
                "path='" + path + '\'' +
                ", sectionName='" + sectionName + '\'' +
                ", size=" + size +
                '}';
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
