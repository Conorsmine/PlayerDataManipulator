package com.conorsmine.net.mojangson.data;

import java.util.Collections;
import java.util.List;

public class NBTPrimitiveListData implements INBTListData<Object>, IPrimitiveData<List<Object>> {

    private final List<Object> data;

    public NBTPrimitiveListData(List<Object> data) {
        this.data = Collections.unmodifiableList(data);
    }

    @Override
    public NBTDataType getType() {
        return NBTDataType.PRIMITIVE_LIST;
    }

    @Override
    public List<Object> getData() {
        return data;
    }

}
