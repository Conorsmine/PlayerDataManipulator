package com.conorsmine.net.mojangson.data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface INBTListData<E> extends INBTData<List<E>> {

    @Override
    default String stringify() {
        return "INBTData{" +
                "type=" + getType().name() +
                ", data=[" + getData().stream().map(Objects::toString).collect(Collectors.joining(", ")) + ']' +
                '}';
    }
}
