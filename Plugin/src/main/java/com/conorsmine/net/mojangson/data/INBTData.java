package com.conorsmine.net.mojangson.data;


public interface INBTData<E> {

    NBTDataType getType();

    E getData();

    default String stringify() {
        return "INBTData{" +
                "type=" + getType().name() +
                ", data=" + getData().toString() +
                '}';
    }
}
