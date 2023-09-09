package com.conorsmine.net.mojangson.data;

import org.jetbrains.annotations.NotNull;

public enum NBTDataType {
    COMPOUND,
    COMPOUND_LIST,
    PRIMITIVE,
    PRIMITIVE_LIST;


    public static boolean isList(final @NotNull NBTDataType dataType) {
        return (dataType == COMPOUND_LIST ||dataType == PRIMITIVE_LIST);
    }
}
